package de.bergwerklabs.friends.api

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.corepackages.AtlantisCache
import de.bergwerklabs.atlantis.api.corepackages.cache.CacheLoadAndGetPacket
import de.bergwerklabs.atlantis.api.corepackages.cache.CachePacket
import de.bergwerklabs.atlantis.api.corepackages.cache.CacheUpdatePacket
import de.bergwerklabs.atlantis.api.corepackages.cache.UpdateAction
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit
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
                responseListener.forEach { listener -> listener.onResponse(packet.requestResponse, packet.sender, packet.receiver) }
            })
    
            service.addListener(FriendInviteRequestPacket::class.java, { packet ->
                requestListener.forEach { listener -> listener.onInvite(packet.sender, packet.receiver) }
            })
        }
        
        @JvmStatic
        fun sendInvite(sender: UUID, receiver: UUID) {
            val info = retrieveFriendInfo(sender)
    
            when {
                info.friendList.any { friendEntry -> friendEntry.friend == receiver }          -> {
                    responseListener.forEach { listeners -> listeners.onResponse(FriendRequestResponse.ALREADY_FRIENDS, sender, receiver) }
                    return
                }
                info.pendingInvites.any { requestEntry -> requestEntry.requester == receiver } -> {
                    responseListener.forEach { listeners -> listeners.onResponse(FriendRequestResponse.ALREADY_REQUESTED, sender, receiver) }
                    return
                }
                info.friendList.size >= 100                                                    -> { // TODO: check if premium
                    responseListener.forEach { listeners -> listeners.onResponse(FriendRequestResponse.FRIEND_LIST_FULL, sender, receiver) }
                    return
                }
                else -> {
                    service.sendPackage(CacheUpdatePacket(sender.toString(), hashSetOf(RequestEntry(getTimestamp(), receiver)), AtlantisCache.PENDING_FRIEND_REQUESTS_CACHE, UpdateAction.ADD))
                    service.sendPackage(FriendInviteRequestPacket(receiver, sender))
                }
            }
        }
        
        @JvmStatic
        fun respondToInvite(sender: UUID, receiver: UUID, response: FriendRequestResponse) {
            if (response == FriendRequestResponse.ACCEPTED)
                service.sendPackage(CacheUpdatePacket(sender.toString(), hashSetOf(FriendEntry(getTimestamp(), receiver)), AtlantisCache.FRIEND_LIST_CACHE, UpdateAction.ADD))
            
            service.sendPackage(CacheUpdatePacket(sender.toString(), hashSetOf(RequestEntry(null, sender)), AtlantisCache.PENDING_FRIEND_REQUESTS_CACHE, UpdateAction.REMOVE))
            service.sendPackage(FriendInviteResponsePacket(sender, receiver, response))
        }
        
        @JvmStatic
        fun removeFriend(from: UUID, toRemove: UUID) {
            service.sendPackage(CacheUpdatePacket(from.toString(), hashSetOf(FriendEntry(getTimestamp(), toRemove)), AtlantisCache.FRIEND_LIST_CACHE, UpdateAction.REMOVE))
        }
        
        @JvmStatic
        fun retrieveFriendInfo(player: UUID): FriendInfo = FriendInfo(this.getFriendlist(player), this.getPendingRequests(player))
        
        @JvmStatic
        fun getPendingRequests(player: UUID): Set<RequestEntry> {
            val packet = service.sendRequestWithFuture(CacheLoadAndGetPacket<UUID>(player.toString(), AtlantisCache.FRIEND_LIST_CACHE), CachePacket::class.java)
                    .get(4, TimeUnit.SECONDS)
            return packet.cacheObject as HashSet<RequestEntry>
        }
    
        @JvmStatic
        fun getFriendlist(player: UUID): Set<FriendEntry> {
            val packet = service.sendRequestWithFuture(CacheLoadAndGetPacket<UUID>(player.toString(), AtlantisCache.PENDING_FRIEND_REQUESTS_CACHE), CachePacket::class.java)
                    .get(4, TimeUnit.SECONDS)
            
            return packet.cacheObject as HashSet<FriendEntry>
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