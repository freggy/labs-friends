package de.bergwerklabs.friends.server.listener

import de.bergwerklabs.atlantis.api.friends.FriendInviteResponse
import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.client.base.util.AtlantisListener

/**
 * Created by Yannic Rieger on 23.10.2017.
 *
 * @author Yannic Rieger
 */
class FriendInviteResponseListener : AtlantisListener<FriendInviteResponse>() {
    
    private val logger = AtlantisLogger.getLogger(this::class.java)
    
    override fun onResponse(pkg: FriendInviteResponse?) {
    
    }
}