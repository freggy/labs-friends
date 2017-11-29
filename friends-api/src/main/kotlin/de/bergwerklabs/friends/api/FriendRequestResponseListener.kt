package de.bergwerklabs.friends.api

import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import java.util.*

/**
 * Created by Yannic Rieger on 29.11.2017.
 * <p>
 * @author Yannic Rieger
 */
interface FriendRequestResponseListener {
    
    fun onResponse(response: FriendRequestResponse, sender: UUID, receiver: UUID)
    
}