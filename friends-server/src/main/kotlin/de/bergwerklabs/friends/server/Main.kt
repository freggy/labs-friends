package de.bergwerklabs.friends.server

import com.google.gson.Gson
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientResponsePacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.atlantis.api.friends.server.FriendlistRequestPacket
import de.bergwerklabs.atlantis.api.friends.server.FriendlistResponsePacket
import de.bergwerklabs.atlantis.api.friends.server.PlayerLoginPacket
import de.bergwerklabs.atlantis.api.friends.server.PlayerLogoutPacket
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

                dao.retrieveFriendsAsync(uuid).thenAccept { friends ->
                    uuidToFriends[uuid] = friends
                }

                dao.retrievePendingInvitesAsync(uuid).thenAccept { pending ->
                    uuidToPending[uuid] = pending
                }

                dao.retrieveSentInvitesAsync(uuid).thenAccept { requested ->
                    uuidToRequested[uuid] = requested
                }
            })

            service.addListener(PlayerLogoutPacket::class.java, { packet ->
                uuidToFriends.remove(packet.uuid)
                uuidToPending.remove(packet.uuid)
                uuidToRequested.remove(packet.uuid)
            })

            service.addListener(FriendInviteClientRequestPacket::class.java, { packet ->
                val sender = packet.sender.uuid
                val receiver = packet.receiver.uuid

                if (uuidToFriends[sender]?.any { it.friend == receiver }!!) {
                    service.sendResponse(FriendInviteServerResponse(packet.sender, packet.receiver, FriendRequestResponse.ALREADY_FRIENDS), packet)
                    return@addListener
                }

                if (uuidToRequested[sender]?.any { it.acceptor == receiver }!!) {
                    service.sendResponse(FriendInviteServerResponse(packet.sender, packet.receiver, FriendRequestResponse.ALREADY_REQUESTED), packet)
                    return@addListener
                }

                // TODO: limit friend list
                
                val timestamp = Timestamp(System.currentTimeMillis())

                uuidToRequested[sender]?.add(RequestEntry(timestamp.toString(), sender, receiver))

                uuidToPending[receiver]?.add(RequestEntry(timestamp.toString(), sender, receiver))
                dao.savePendingRequestAsync(packet.receiver.uuid, packet.sender.uuid, timestamp)
            })

            service.addListener(FriendInviteClientResponsePacket::class.java, { packet ->
                val sender = packet.sender.uuid
                val receiver = packet.receiver.uuid

                if (packet.response == FriendRequestResponse.DENIED) {
                    dao.deletePendingRequestAsync(sender, receiver)
                    uuidToPending[sender]?.removeIf { entry ->
                        entry.requester == receiver && entry.acceptor == sender
                    }

                    uuidToRequested[receiver]?.removeIf { entry ->
                        entry.requester == receiver && entry.acceptor == sender
                    }
                }
                else if (packet.response == FriendRequestResponse.ACCEPTED) {
                    val timestamp = Timestamp(System.currentTimeMillis())

                    dao.createFriendshipAsync(sender, receiver, timestamp)
                    dao.createFriendshipAsync(receiver, sender, timestamp)

                    uuidToFriends[sender]?.add(FriendEntry(timestamp.toString(), receiver, sender))
                    uuidToFriends[receiver]?.add(FriendEntry(timestamp.toString(), sender, receiver))
                    
                    // Send SUCCESS back so the client knows the request has been successfully processed.
                    service.sendResponse(FriendInviteServerResponse(packet.sender, packet.receiver, FriendRequestResponse.SUCCESS), packet)
                    service.sendPackage(FriendInviteServerResponse(packet.sender, packet.receiver, packet.response))
                }
            })
            
            service.addListener(FriendlistRequestPacket::class.java, { packet ->
                val set = uuidToFriends[packet.uuid]?.map { friendEntry ->
                    Friend(PlayerResolver.resolveUuidToName(friendEntry.friend), friendEntry.created)
                }!!.toHashSet()
                service.sendResponse(FriendlistResponsePacket(packet.uuid, set), packet)
            })
        }
    }
}