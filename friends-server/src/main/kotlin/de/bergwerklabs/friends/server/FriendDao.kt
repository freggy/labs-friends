package de.bergwerklabs.friends.server

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.framework.commons.database.tablebuilder.Database
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

/**
 * Provides asynchronous methods for interacting with the database.
 *
 * @param database Object containing database information.
 * @author Yannic Rieger
 */
class FriendDao(private val database: Database) {

    private val executor = Executors.newFixedThreadPool(10)

    private val DELETE_FRIENDSHIP_QUERY = "DELETE FROM netsyn.friendships WHERE uuid1 = ? OR uuid2 = ?"
    private val GET_FRIENDSHIPS_QUERY = "SELECT * FROM netsyn.friendships WHERE uuid2 = ? OR uuid1 = ?"
    private val CREATE_FRIENDSHIP_QUERY = "INSERT INTO netsyn.friendships (uuid1, uuid2, created) VALUES (?, ?, ?)"

    private val DELETE_PENDING_INVITES_QUERY = "DELETE FROM netsyn.friend_requests WHERE acceptor = ? AND requester ?"
    private val GET_PENDING_INVITES_QUERY = "SELECT requester FROM netsyn.friend_requests WHERE acceptor = ?"
    private val CREATE_PENDING_INVITES_QUERY = "INSERT INTO netsyn.friend_requests (requester, acceptor, created) VALUES (?, ?, ?)"

    private val GET_SENT_INVITES_QUERY = "SELECT acceptor FROM netsyn.friend_requests WHERE requester = ?"

    /**
     * Gets the friend list of asynchronously from the database.
     *
     * @param uuid [UUID] of the player.
     * @return [CompletableFuture] containing the result.
     */
    fun retrieveFriendsAsync(uuid: UUID): CompletableFuture<MutableSet<FriendEntry>> {
        val friends = hashSetOf<FriendEntry>()
        val future = CompletableFuture<MutableSet<FriendEntry>>()

        this.executor.submit {
            val statement = database.prepareStatement(GET_FRIENDSHIPS_QUERY)
            val result = statement.execute(uuid.toString())
            statement.close()

            result.rows.forEach { row ->
                val uuid1 = UUID.fromString(row.getString("uuid1"))
                val uuid2 = UUID.fromString(row.getString("uuid2"))
                val created = row.getTimestamp("created").toString()

                // Since uuid1 or uuid2 can be the players UUID to this check.
                if (uuid1 == uuid) {
                    friends.add(FriendEntry(created, uuid2, uuid1))
                } else friends.add(FriendEntry(created, uuid1, uuid2))
            }
            future.complete(friends)
        }
        return future
    }

    /**
     * Deletes a friend from the database asynchronously.
     *
     * @param deleteFrom [UUID] of the player to delete the friend from.
     * @param toDelete   [UUID] of the player to remove.
     */
    fun deleteFriendAsync(deleteFrom: UUID, toDelete: UUID) {
        this.executor.submit {
            val statement = this.database.prepareStatement(DELETE_FRIENDSHIP_QUERY)
            statement.execute(deleteFrom.toString(), toDelete.toString())
            statement.close()
        }
    }

    /**
     * Creates a new friendship entry in the database.
     *
     * @param responder [UUID] of the player who responded to the invite.
     * @param requester [UUID] of the player who requested the invite.
     * @param created   [Timestamp] when the request go accepted.
     */
    fun createFriendshipAsync(responder: UUID, requester: UUID, created: Timestamp) {
        this.executor.submit {
            val statement = this.database.prepareStatement(CREATE_FRIENDSHIP_QUERY)
            statement.execute(responder.toString(), requester.toString(), created)
            statement.close()
        }
    }

    /**
     * Saves a pending friend request to the database.
     *
     * @param responder [UUID] of the player who should respond.
     * @param requester [UUID] of the player who sent the invite.
     * @param created   [Timestamp] when the request got initiated.
     */
    fun savePendingRequestAsync(responder: UUID, requester: UUID, created: Timestamp) {
        this.executor.submit {
            val statement = this.database.prepareStatement(CREATE_PENDING_INVITES_QUERY)
            statement.execute(responder.toString(), requester.toString(), created)
            statement.close()
        }
    }

    /**
     * Deletes a pending request from the database.
     *
     * @param responder [UUID] of the player who responded.
     * @param requester [UUID] of the player who requested.
     */
    fun deletePendingRequestAsync(responder: UUID, requester: UUID) {
        this.executor.submit {
            val statement = this.database.prepareStatement(DELETE_PENDING_INVITES_QUERY)
            statement.execute(responder.toString(), requester.toString())
            statement.close()
        }
    }

    /**
     * Retrieves the open friend requests of a player.
     *
     * @param acceptor [UUID] of the player that needs to respond to the invites.
     */
    fun retrievePendingInvitesAsync(acceptor: UUID): CompletableFuture<MutableSet<RequestEntry>> {
        val future = CompletableFuture<MutableSet<RequestEntry>>()

        this.executor.submit {
            future.complete(this.retrievePending(GET_PENDING_INVITES_QUERY, acceptor))
        }
        return future
    }

    /**
     * Retrieves the invites sent by a specific player.
     *
     * @param sender [UUID] of the player.
     */
    fun retrieveRequestedInvitesAsync(sender: UUID): CompletableFuture<MutableSet<RequestEntry>> {
        val future = CompletableFuture<MutableSet<RequestEntry>>()

        this.executor.submit {
            future.complete(this.retrievePending(GET_SENT_INVITES_QUERY, sender))
        }
        return future
    }

    /**
     * Retrieves data from the `netsyn.friend_requests` database.
     *
     * @param query Query to be performed on the database.
     * @param uuid [UUID] of an entry, needs to be either `acceptor` or `requester`.
     */
    private fun retrievePending(query: String, uuid: UUID): MutableSet<RequestEntry> {
        val set = hashSetOf<RequestEntry>()
        val statement = this.database.prepareStatement(query)
        val result = statement.execute(uuid)
        statement.close()

        result.rows.forEach { row ->
            val requesterString = row.getString("requester")
            val acceptorString = row.getString("acceptor")

            if (acceptorString != null && requesterString != null) {
                val requester = UUID.fromString(requesterString)
                val acc = UUID.fromString(acceptorString)
                val created = row.getTimestamp("created").toString()
                set.add(RequestEntry(created, requester, acc))
            }
        }
        return set
    }
}