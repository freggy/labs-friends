package de.bergwerklabs.friends.api

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.corepackages.cache.CachePacket
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientResponsePacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.atlantis.api.friends.server.*
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashSet

/**
 * Created by Yannic Rieger on 01.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsApi {
    
    companion object {
        
        private val service = AtlantisPackageService(
                FriendInviteResponsePacket::class.java,
                FriendInviteRequestPacket::class.java,
                CachePacket::class.java)
        
        private val responseListener = HashSet<FriendRequestResponseListener>()
        private val requestListener = HashSet<FriendInviteListener>()
        
        init {
            service.addListener(FriendInviteResponsePacket::class.java, { packet ->
                responseListener.forEach { listener -> listener.onResponse(packet.requestResponse, packet.sender.uuid, packet.receiver.uuid) }
            })
    
            service.addListener(FriendInviteRequestPacket::class.java, { packet ->
                requestListener.forEach { listener -> listener.onInvite(packet.sender.uuid, packet.receiver.uuid) }
            })
        }

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
        fun getPendingInvites(player: UUID): CompletableFuture<MutableSet<RequestEntry>> {
            val future = CompletableFuture<MutableSet<RequestEntry>>()
            service.sendPackage(PendingInvitesRequestPacket(player), PendingInvitesResponsePacket::class.java, AtlantisPackageService.Callback {
                future.complete(it.pendingEntries)
            })
            return future
        }
        
        
        fun respondToInvite(sender: PlayerNameToUuidMapping, receiver: PlayerNameToUuidMapping, response: FriendRequestResponse): CompletableFuture<InviteResponseData> {
            val future = CompletableFuture<InviteResponseData>()
            service.sendPackage(FriendInviteClientResponsePacket(receiver, sender, response), FriendInviteServerResponse::class.java, AtlantisPackageService.Callback {
                future.complete(InviteResponseData(it.sender, it.receiver, it.response))
            })
            return future
        }
        
        @JvmStatic
        fun sendInvite(sender: PlayerNameToUuidMapping, receiver: PlayerNameToUuidMapping): CompletableFuture<InviteResponseData> {
            val future = CompletableFuture<InviteResponseData>()
            service.sendPackage(FriendInviteClientRequestPacket(sender, receiver), FriendInviteServerResponse::class.java, AtlantisPackageService.Callback {
                future.complete(InviteResponseData(it.sender, it.receiver, it.response))
            })
            return future
        }


        @JvmStatic
        fun registerResponseListener(listener: FriendRequestResponseListener) {
            responseListener.add(listener)
        }
    
        @JvmStatic
        fun registerInviteListener(listener: FriendInviteListener) {
            requestListener.add(listener)
        }
        
        private fun getTimestamp() = Timestamp(System.currentTimeMillis()).toString()
    }
}