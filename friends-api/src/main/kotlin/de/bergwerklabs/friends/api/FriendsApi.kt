package de.bergwerklabs.friends.api

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientResponsePacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerRequest
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.atlantis.api.friends.server.*
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Created by Yannic Rieger on 01.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsApi {
    
    companion object {
        
        private val service = AtlantisPackageService(
            FriendInviteServerResponse::class.java,
            FriendlistResponsePacket::class.java,
            FriendInviteServerRequest::class.java,
            PendingInvitesResponsePacket::class.java
        )
        
        @JvmStatic
        fun sendPlayerLogin(player: UUID) {
            service.sendPackage(PlayerLoginPacket(player))
        }

        @JvmStatic
        fun sendPlayerLogout(player: UUID) {
            service.sendPackage(PlayerLogoutPacket(player))
        }

        @JvmStatic
        fun getFriendList(player: UUID): CompletableFuture<MutableSet<Friend>> {
            val future = CompletableFuture<MutableSet<Friend>>()
            service.sendPackage(FriendlistRequestPacket(player), FriendlistResponsePacket::class.java, AtlantisPackageService.Callback {
                future.complete(it.friends)
            })
            return future
        }
        
        @JvmStatic
        fun getPendingInvites(player: UUID): CompletableFuture<MutableSet<Request>> {
            val future = CompletableFuture<MutableSet<Request>>()
            service.sendPackage(PendingInvitesRequestPacket(player), PendingInvitesResponsePacket::class.java, AtlantisPackageService.Callback {
                future.complete(it.requestEntries)
            })
            return future
        }
        
        @JvmStatic
        fun sendInvite(sender: PlayerNameToUuidMapping, receiver: PlayerNameToUuidMapping): CompletableFuture<InviteResponseData> {
            val future = CompletableFuture<InviteResponseData>()
            service.sendPackage(FriendInviteClientRequestPacket(receiver, sender), FriendInviteServerResponse::class.java, AtlantisPackageService.Callback {
                future.complete(InviteResponseData(it.sender, it.receiver, it.response))
            })
            return future
        }
    
        @JvmStatic
        fun removeFriend(toRemove: PlayerNameToUuidMapping, removeFrom: PlayerNameToUuidMapping) {
            service.sendPackage(FriendRemovePacket(toRemove, removeFrom))
        }
    
    
        fun respondToInvite(sender: PlayerNameToUuidMapping, receiver: PlayerNameToUuidMapping, response: FriendRequestResponse): CompletableFuture<InviteResponseData> {
            val future = CompletableFuture<InviteResponseData>()
            service.sendPackage(FriendInviteClientResponsePacket(receiver, sender, response), FriendInviteServerResponse::class.java, AtlantisPackageService.Callback {
                future.complete(InviteResponseData(it.sender, it.receiver, it.response))
            })
            return future
        }
    }
}