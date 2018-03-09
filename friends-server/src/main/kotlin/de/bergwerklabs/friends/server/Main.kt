package de.bergwerklabs.friends.server

import com.google.gson.Gson
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.api.friends.server.PlayerLoginPacket
import de.bergwerklabs.atlantis.api.friends.server.PlayerLogoutPacket
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

            service.addListener(FriendInviteRequestPacket::class.java, { packet ->
                val sender = packet.sender.uuid
                val receiver = packet.receiver.uuid

                if (uuidToFriends[sender]?.any { it.friend == receiver }!!) {
                    service.sendResponse(FriendInviteResponsePacket(packet.sender, packet.receiver, FriendRequestResponse.ALREADY_FRIENDS), packet)
                    return@addListener
                }

                if (uuidToRequested[sender]?.any { it.acceptor == receiver }!!) {
                    service.sendResponse(FriendInviteResponsePacket(packet.sender, packet.receiver, FriendRequestResponse.ALREADY_REQUESTED), packet)
                    return@addListener
                }

                val timestamp = Timestamp(System.currentTimeMillis())

                uuidToRequested[sender]?.add(RequestEntry(timestamp.toString(), sender, receiver))

                uuidToPending[receiver]?.add(RequestEntry(timestamp.toString(), sender, receiver))
                dao.savePendingRequestAsync(packet.receiver.uuid, packet.sender.uuid, timestamp)
            })

            service.addListener(FriendInviteResponsePacket::class.java, { packet ->
                val sender = packet.sender.uuid
                val receiver = packet.receiver.uuid

                if (packet.requestResponse == FriendRequestResponse.DENIED) {
                    dao.deletePendingRequestAsync(sender, receiver)
                    uuidToPending[sender]?.removeIf { entry ->
                        entry.requester == receiver && entry.acceptor == sender
                    }

                    uuidToRequested[receiver]?.removeIf { entry ->
                        entry.requester == receiver && entry.acceptor == sender
                    }
                }
                else if (packet.requestResponse == FriendRequestResponse.ACCEPTED) {
                    val timestamp = Timestamp(System.currentTimeMillis())

                    dao.createFriendshipAsync(sender, receiver, timestamp)
                    dao.createFriendshipAsync(receiver, sender, timestamp)

                    uuidToFriends[sender]?.add(FriendEntry(timestamp.toString(), receiver, sender))
                    uuidToFriends[receiver]?.add(FriendEntry(timestamp.toString(), sender, receiver))
                }
            })
            
            println("hellooooo")
            
            dao.retrieveFriendsAsync(UUID.fromString("92de217b-8b2b-403b-86a5-fe26fa3a9b5f")).thenAccept { entry ->
                entry.forEach { println(it.friend) }
            }
            
        }
    }
}