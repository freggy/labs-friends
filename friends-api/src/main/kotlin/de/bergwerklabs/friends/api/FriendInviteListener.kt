package de.bergwerklabs.friends.api

import java.util.*

/**
 * Created by Yannic Rieger on 29.11.2017.
 * <p>
 * @author Yannic Rieger
 */
interface FriendInviteListener {
    
    fun onInvite(sender: UUID, invited: UUID)
    
}