package de.bergwerklabs.friends.server

import com.google.gson.Gson
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientResponsePacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.atlantis.api.friends.server.*
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.database.tablebuilder.Database
import de.bergwerklabs.framework.commons.database.tablebuilder.DatabaseType
import java.io.FileReader
import java.sql.Timestamp
import java.util.*

class Main {
    
    companion object {
        
        @JvmStatic
        fun main(args: Array<String>) {
            val config = Gson().fromJson(FileReader("config.json"), Config::class.java)
            
            val dao = FriendDao(
                Database(
                    DatabaseType.MySQL,
                    config.host,
                    config.database,
                    config.user,
                    config.password
                )
            )
            
            service.addListener(PlayerLoginPacket::class.java, { packet ->
                val uuid = packet.uuid
                println("Loading information for $uuid")
                uuidToFriends[uuid] = dao.retrieveFriendsAsync(uuid)
                uuidToPending[uuid] = dao.retrievePendingInvitesAsync(uuid)
                uuidToRequested[uuid] = dao.retrieveSentInvitesAsync(uuid)
            })
            
            service.addListener(PlayerLogoutPacket::class.java, { packet ->
                println("Unloading information for ${packet.uuid}")
                uuidToFriends.remove(packet.uuid)
                uuidToPending.remove(packet.uuid)
                uuidToRequested.remove(packet.uuid)
            })
            
            service.addListener(PendingInvitesRequestPacket::class.java, { packet ->
                // invoke join() and get() explicitly because I know that these values are present.
                val set = uuidToPending[packet.uuid]?.join()?.map { pending ->
                    Request(PlayerResolver.resolveUuidToNameAsync(pending.requester).join().get(), pending.created)
                }?.toHashSet()
                service.sendResponse(PendingInvitesResponsePacket(set), packet)
            })
            
            service.addListener(FriendRemovePacket::class.java, { packet ->
                // invoke join() and get() explicitly because I know that these values are present.
                println("Deleting friendship of ${packet.removeFrom} and ${packet.toRemove}")
                val toRemove = PlayerResolver.resolveNameToUuidAsync(packet.toRemove.name).join().get()
                dao.deleteFriendAsync(packet.removeFrom.uuid, toRemove.uuid)
                uuidToFriends[packet.removeFrom.uuid]?.join()?.removeIf { friend -> friend.friend == toRemove.uuid }
                uuidToFriends[toRemove.uuid]?.join()?.removeIf { friend -> friend.friend == packet.removeFrom.uuid }
            })
            
            service.addListener(FriendInviteClientRequestPacket::class.java, { packet ->
                // since UUID is not known request it.
                PlayerResolver.resolveNameToUuidAsync(packet.receiver.name).thenAccept { opt ->
                    if (!opt.isPresent) {
                        service.sendResponse(
                            FriendInviteServerResponse(
                                packet.sender, packet.receiver, FriendRequestResponse.UNKNOWN_NAME
                            ), packet
                        )
                        return@thenAccept
                    }
                    
                    val receiver = opt.get()
                    val receiverUuid = receiver.uuid
                    val senderUuid = packet.sender.uuid
    
                    uuidToFriends[senderUuid]?.let { friends ->
                        if (friends.join().any { it.friend == receiverUuid }) {
                            service.sendResponse(
                                FriendInviteServerResponse(
                                    packet.sender, packet.receiver, FriendRequestResponse.ALREADY_FRIENDS
                                ), packet
                            )
                            return@thenAccept
                        }
                    }
    
                    uuidToRequested[senderUuid]?.let { requested ->
                        if (requested.join().any { it.acceptor == receiverUuid }) {
                            service.sendResponse(
                                FriendInviteServerResponse(
                                    packet.sender, packet.receiver, FriendRequestResponse.ALREADY_REQUESTED
                                ), packet
                            )
                            return@thenAccept
                        }
                    }
    
                    // Since both have invited each other, this implies that they want to be friends.
                    uuidToPending[senderUuid]?.let { requested ->
                        if (requested.join().any { it.requester == receiverUuid }) {
                            service.sendPackage(
                                FriendInviteServerResponse(
                                    receiver, packet.sender, FriendRequestResponse.ACCEPTED
                                )
                            )
                            // Send SUCCESS back so the client knows the request has been successfully processed.
                            service.sendResponse(
                                FriendInviteServerResponse(receiver, packet.sender, FriendRequestResponse.SUCCESS), packet
                            )
                            val timestamp = Timestamp(System.currentTimeMillis())
                            dao.createFriendshipAsync(senderUuid, receiverUuid, timestamp)
            
                            this.removePending(dao, senderUuid, receiverUuid)
                            this.removePending(dao, receiverUuid, senderUuid)
            
                            uuidToFriends[senderUuid]?.join()?.add(FriendEntry(timestamp.toString(), receiverUuid, senderUuid))
                            uuidToFriends[receiverUuid]?.join()?.add(FriendEntry(timestamp.toString(), senderUuid, receiverUuid))
                            return@thenAccept
                        }
                    }
    
                    // TODO: limit friend list
    
                    val timestamp = Timestamp(System.currentTimeMillis())
    
                    uuidToRequested[senderUuid]?.join()?.add(RequestEntry(timestamp.toString(), senderUuid, receiver.uuid))
                    uuidToPending[receiver.uuid]?.join()?.add(RequestEntry(timestamp.toString(), senderUuid, receiver.uuid))
    
                    dao.savePendingRequestAsync(receiver.uuid, senderUuid, timestamp)
                    service.sendPackage(FriendServerInviteRequestPacket(receiver, packet.sender))
                }
            })
            
            service.addListener(FriendInviteClientResponsePacket::class.java, { packet ->
                PlayerResolver.resolveNameToUuidAsync(packet.receiver.name).thenAccept { opt ->
                    if (!opt.isPresent) {
                        service.sendResponse(
                            FriendInviteServerResponse(
                                packet.sender, packet.receiver, FriendRequestResponse.UNKNOWN_NAME
                            ), packet
                        )
                        return@thenAccept
                    }
    
                    val receiver = opt.get()
                    val receiverUuid = receiver.uuid
                    val senderUuid = packet.sender.uuid
    
                    this.removePending(dao, senderUuid, receiverUuid)
    
                    if (packet.response == FriendRequestResponse.ACCEPTED) {
                        val timestamp = Timestamp(System.currentTimeMillis())
        
                        dao.createFriendshipAsync(senderUuid, receiverUuid, timestamp)
        
                        uuidToFriends[senderUuid]?.join()?.add(FriendEntry(timestamp.toString(), receiverUuid, senderUuid))
                        uuidToFriends[receiverUuid]?.join()?.add(FriendEntry(timestamp.toString(), senderUuid, receiverUuid))
        
                        // Send SUCCESS back so the client knows the request has been successfully processed.
                        service.sendResponse(
                            FriendInviteServerResponse(packet.sender, receiver, FriendRequestResponse.SUCCESS), packet
                        )
                        service.sendPackage(FriendInviteServerResponse(receiver, packet.sender, packet.response))
                    }
                }
            })
            
            service.addListener(FriendlistRequestPacket::class.java, { packet ->
                val set = uuidToFriends[packet.uuid]?.join()?.map { friendEntry ->
                    Friend(PlayerResolver.resolveUuidToNameAsync(friendEntry.friend).join().get(), friendEntry.created)
                }?.toHashSet()
                service.sendResponse(FriendlistResponsePacket(packet.uuid, set), packet)
            })
        }
    
        private fun removePending(dao: FriendDao, acceptor: UUID, requester: UUID) {
            dao.deletePendingRequestAsync(acceptor, requester)
            uuidToPending[acceptor]?.join()?.removeIf { entry ->
                entry.requester == requester && entry.acceptor == acceptor
            }
        
            uuidToRequested[requester]?.join()?.removeIf { entry ->
                entry.requester == requester && entry.acceptor == acceptor
            }
        }
    }
}