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
                
                // TODO: store futures to remove the waiting in client
                val uuid = packet.uuid
                println("Loading information for $uuid")
                
                dao.retrieveFriendsAsync(uuid).thenAccept { friends ->
                    uuidToFriends[uuid] = friends
                    println("Loaded friends for $uuid with size ${friends.size}")
                }
                
                dao.retrievePendingInvitesAsync(uuid).thenAccept { pending ->
                    uuidToPending[uuid] = pending
                    println("Loaded pending invites for $uuid with size ${pending.size}")
                }
                
                dao.retrieveSentInvitesAsync(uuid).thenAccept { requested ->
                    uuidToRequested[uuid] = requested
                    println("Loaded sent invites for $uuid with size ${requested.size}")
                }
            })
            
            service.addListener(PlayerLogoutPacket::class.java, { packet ->
                println("Unloading information for ${packet.uuid}")
                uuidToFriends.remove(packet.uuid)
                uuidToPending.remove(packet.uuid)
                uuidToRequested.remove(packet.uuid)
            })
            
            service.addListener(PendingInvitesRequestPacket::class.java, { packet ->
                val set = uuidToPending[packet.uuid]?.map { pending ->
                    Request(PlayerResolver.resolveUuidToName(pending.requester), pending.created)
                }?.toHashSet()
                service.sendResponse(PendingInvitesResponsePacket(set), packet)
            })
            
            service.addListener(FriendRemovePacket::class.java, { packet ->
                println("Deleting friendship of ${packet.removeFrom} and ${packet.toRemove}")
                val toRemove = PlayerResolver.resolveNameToUuid(packet.toRemove.name)
                dao.deleteFriendAsync(packet.removeFrom.uuid, toRemove.uuid)
                uuidToFriends[packet.removeFrom.uuid]?.removeIf { friend -> friend.friend == toRemove.uuid }
                uuidToFriends[toRemove.uuid]?.removeIf { friend -> friend.friend == packet.removeFrom.uuid }
            })
            
            service.addListener(FriendInviteClientRequestPacket::class.java, { packet ->
                val receiver = PlayerResolver.resolveNameToUuid(
                    packet.receiver.name
                ) // since UUID is not known request it.
                val receiverUuid = receiver.uuid
                val senderUuid = packet.sender.uuid
                
                uuidToFriends[senderUuid]?.let { friends ->
                    if (friends.any { it.friend == receiverUuid }) {
                        service.sendResponse(
                            FriendInviteServerResponse(
                                packet.sender, packet.receiver, FriendRequestResponse.ALREADY_FRIENDS
                            ), packet
                        )
                        return@addListener
                    }
                }
                
                uuidToRequested[senderUuid]?.let { requested ->
                    if (requested.any { it.acceptor == receiverUuid }) {
                        service.sendResponse(
                            FriendInviteServerResponse(
                                packet.sender, packet.receiver, FriendRequestResponse.ALREADY_REQUESTED
                            ), packet
                        )
                        return@addListener
                    }
                }
                
                // Since both have invited each other, this implies that they want to be friends.
                uuidToPending[senderUuid]?.let { requested ->
                    if (requested.any { it.requester == receiverUuid }) {
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
                        
                        uuidToFriends[senderUuid]?.add(FriendEntry(timestamp.toString(), receiverUuid, senderUuid))
                        uuidToFriends[receiverUuid]?.add(FriendEntry(timestamp.toString(), senderUuid, receiverUuid))
                        return@addListener
                    }
                }
                
                // TODO: limit friend list
                
                val timestamp = Timestamp(System.currentTimeMillis())
                
                uuidToRequested[senderUuid]?.add(RequestEntry(timestamp.toString(), senderUuid, receiver.uuid))
                uuidToPending[receiver.uuid]?.add(RequestEntry(timestamp.toString(), senderUuid, receiver.uuid))
                
                dao.savePendingRequestAsync(receiver.uuid, senderUuid, timestamp)
                service.sendPackage(FriendServerInviteRequestPacket(receiver, packet.sender))
            })
            
            service.addListener(FriendInviteClientResponsePacket::class.java, { packet ->
                val receiver = PlayerResolver.resolveNameToUuid(
                    packet.receiver.name
                ) // since UUID is not known request it.
                val receiverUuid = receiver.uuid
                val senderUuid = packet.sender.uuid
                
                this.removePending(dao, senderUuid, receiverUuid)
                
                if (packet.response == FriendRequestResponse.ACCEPTED) {
                    val timestamp = Timestamp(System.currentTimeMillis())
                    
                    dao.createFriendshipAsync(senderUuid, receiverUuid, timestamp)
                    
                    uuidToFriends[senderUuid]?.add(FriendEntry(timestamp.toString(), receiverUuid, senderUuid))
                    uuidToFriends[receiverUuid]?.add(FriendEntry(timestamp.toString(), senderUuid, receiverUuid))
                    
                    // Send SUCCESS back so the client knows the request has been successfully processed.
                    service.sendResponse(
                        FriendInviteServerResponse(packet.sender, receiver, FriendRequestResponse.SUCCESS), packet
                    )
                    service.sendPackage(FriendInviteServerResponse(receiver, packet.sender, packet.response))
                }
            })
            
            service.addListener(FriendlistRequestPacket::class.java, { packet ->
                val set = uuidToFriends[packet.uuid]?.map { friendEntry ->
                    Friend(PlayerResolver.resolveUuidToName(friendEntry.friend), friendEntry.created)
                }?.toHashSet()
                service.sendResponse(FriendlistResponsePacket(packet.uuid, set), packet)
            })
        }
    
        private fun removePending(dao: FriendDao, acceptor: UUID, requester: UUID) {
            dao.deletePendingRequestAsync(acceptor, requester)
            uuidToPending[acceptor]?.removeIf { entry ->
                entry.requester == requester && entry.acceptor == acceptor
            }
        
            uuidToRequested[requester]?.removeIf { entry ->
                entry.requester == requester && entry.acceptor == acceptor
            }
        }
    }
}