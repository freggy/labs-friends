package de.bergwerklabs.friends.server

import de.bergwerklabs.atlantis.api.corepackages.client.ClientConnectPackage
import de.bergwerklabs.atlantis.api.friends.FriendInviteRequest
import de.bergwerklabs.atlantis.api.friends.FriendInviteResponse
import de.bergwerklabs.atlantis.api.logging.AtlantisLogger
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.friends.server.listener.ClientConnectListener
import de.bergwerklabs.friends.server.listener.FriendInviteRequestListener
import de.bergwerklabs.friends.server.listener.FriendInviteResponseListener

val packageService = AtlantisPackageService(ClientConnectPackage::class.java, FriendInviteRequest::class.java, FriendInviteResponse::class.java)

/**
 * Created by Yannic Rieger on 23.10.2017.
 *
 * @author Yannic Rieger
 */
class FriendsServer {
    
    companion object {
        
        private val logger = AtlantisLogger.getLogger(this::class.java)
        
        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("Starting bergwerkLABS friend system server.")
            logger.info("Adding listeners...")
            packageService.addListener(ClientConnectPackage::class.java, { pkg -> ClientConnectListener().onResponse(pkg) })
            packageService.addListener(FriendInviteRequest::class.java,  { pkg -> FriendInviteRequestListener().onResponse(pkg) })
            packageService.addListener(FriendInviteResponse::class.java, { pkg -> FriendInviteResponseListener().onResponse(pkg) })
        }
    }
}