package de.bergwerklabs.friends.server

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.framework.commons.database.tablebuilder.Database
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class FriendDao(private val database: Database) {

    private val executor = Executors.newFixedThreadPool(10)

    private val DELETE_FRIENDSHIP_QUERY = "DELETE FROM netsyn.friendships WHERE uuid1 = ? OR uuid2 = ?"
    private val GET_FRIENDS_QUERY = "SELECT * FROM netsyn.friendships WHERE uuid2 = ? OR uuid1 = ?"
    private val CREATE_FRIENDSHIP_QUERY = "INSERT INTO netsyn.friendships (uuid1, uuid2, created) VALUES (?, ?, ?)"

    private val DELETE_PENDING_INVITES_QUERY = "DELETE FROM netsyn.friend_requests WHERE acceptor = ? AND requester ?"
    private val GET_PENDING_INVITES_QUERY = "SELECT requester FROM netsyn.friend_requests WHERE acceptor = ?"
    private val CREATE_PENDING_INVITES_QUERY = "INSERT INTO netsyn.friend_requests (requester, acceptor, created) VALUES (?, ?, ?)"

    private val DELETE_REQUESTED_ENTRY_QUERY = "DELETE FROM friend_requests_sent WHERE sender = ? AND receiver ?"
    private val GET_REQUESTED_ENTRY_QUERY = "SELECT receiver FROM friend_requests_sent WHERE sender = ?"
    private val CREATE_REQUESTED_ENTRY_QUERY = "INSERT INTO friend_requests_sent (sender, receiver, created) VALUES (?, ?, ?)"


    fun retrieveFriendsAsync(uuid: UUID): CompletableFuture<MutableSet<FriendEntry>> {
        val friends = hashSetOf<FriendEntry>()
        val future = CompletableFuture<MutableSet<FriendEntry>>()

        this.executor.submit {
            val statement = database.prepareStatement(GET_FRIENDS_QUERY)
            val result = statement.execute(uuid.toString())
            statement.close()

            result.rows.forEach { row ->
                val uuid1 = UUID.fromString(row.getString("uuid1"))
                val uuid2 = UUID.fromString(row.getString("uuid2"))
                val created = row.getTimestamp("created").toString()

                // Since uuid1 or uuid2 can be the players UUID to this check.
                if (uuid1 == uuid) {
                    friends.add(FriendEntry(created, uuid2, uuid1))
                }
                else friends.add(FriendEntry(created, uuid1, uuid2))
            }
            future.complete(friends)
        }
        return future
    }

    fun deleteFriendAsync(deleteFrom: UUID, toDelete: UUID) {
        this.executor.submit {
            val statement = this.database.prepareStatement(DELETE_FRIENDSHIP_QUERY)
            statement.execute(deleteFrom.toString(), toDelete.toString())
            statement.close()
        }
    }

    fun createFriendshipAsync(responder: UUID, requester: UUID, created: Timestamp) {
        this.executor.submit {
            val statement = this.database.prepareStatement(CREATE_FRIENDSHIP_QUERY)
            statement.execute(responder.toString(), requester.toString(), created)
            statement.close()
        }
    }

    fun savePendingRequestAsync(responder: UUID, requester: UUID, created: Timestamp) {
        this.executor.submit {
            val statement = this.database.prepareStatement(CREATE_PENDING_INVITES_QUERY)
            statement.execute(responder.toString(), requester.toString(), created)
            statement.close()
        }
    }

    fun deletePendingRequestAsync(responder: UUID, requester: UUID) {
        this.executor.submit {
            val statement = this.database.prepareStatement(DELETE_PENDING_INVITES_QUERY)
            statement.execute(responder.toString(), requester.toString())
            statement.close()
        }
    }

    fun retrievePendingInvitesAsync(acceptor: UUID): CompletableFuture<MutableSet<RequestEntry>> {
        val pending = hashSetOf<RequestEntry>()
        val future = CompletableFuture<MutableSet<RequestEntry>>()

        this.executor.submit {
            val statement = this.database.prepareStatement(GET_PENDING_INVITES_QUERY)
            val result = statement.execute(acceptor.toString())
            statement.close()

            result.rows.forEach { row ->
                val requesterString = row.getString("requester")
                val acceptorString = row.getString("acceptor")

                if (acceptorString != null && requesterString != null) {
                    val requester = UUID.fromString(requesterString)
                    val acc = UUID.fromString(acceptorString)
                    val created = row.getTimestamp("created").toString()
                    pending.add(RequestEntry(created, requester, acc))
                }
            }
        }
        return future
    }

    fun createRequestedEntry(sender: UUID, receiver: UUID, timestamp: Timestamp) {
        this.executor.submit {

        }
    }

    fun deleteRequestedEntry(sender: UUID, reciever: UUID) {
        this.executor.submit {

        }
    }

    fun retrieveRequestedPlayersAsync(sender: UUID): CompletableFuture<MutableSet<RequestEntry>> {

    }
}