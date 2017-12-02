package de.bergwerklabs.friends.api

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.corepackages.AtlantisCache
import de.bergwerklabs.atlantis.api.corepackages.cache.CacheLoadAndGetPacket
import de.bergwerklabs.atlantis.api.corepackages.cache.CachePacket
import de.bergwerklabs.atlantis.api.friends.*
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
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
        
        private val service = AtlantisPackageService(FriendInviteResponsePacket::class.java, CachePacket::class.java)
        private val responseListener = HashSet<FriendRequestResponseListener>()
        private val requestListener = HashSet<FriendInviteListener>()
        
        init {
            service.addListener(FriendInviteResponsePacket::class.java, { packet ->
                responseListener.forEach { listener -> listener.onResponse(packet.requestResponse, packet.sender, packet.receiver) }
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
                else -> service.sendPackage(FriendInviteRequestPacket(sender, receiver))
            }
        }
        
        @JvmStatic
        fun respondToInvite(sender: UUID, receiver: UUID, response: FriendRequestResponse) {
            service.sendPackage(FriendInviteResponsePacket(sender, receiver, response))
        }
        
        @JvmStatic
        fun removeFriend(from: UUID, toRemove: UUID) {
            // TODO: cache packet
            //service.sendPackage(RemoveFriendPacket(toRemove, from))
        }
        
        @JvmStatic
        fun retrieveFriendInfo(player: UUID): FriendInfo {
            val packetFl = service.sendRequestWithFuture(CacheLoadAndGetPacket<UUID>(player.toString(), AtlantisCache.FRIEND_LIST_CACHE), CachePacket::class.java)
                                     .get(4, TimeUnit.SECONDS)
    
            val packetPr = service.sendRequestWithFuture(CacheLoadAndGetPacket<UUID>(player.toString(), AtlantisCache.PENDING_FRIEND_REQUESTS_CACHE), CachePacket::class.java)
                    .get(4, TimeUnit.SECONDS)
            
            return FriendInfo(packetFl.cacheObject as HashSet<FriendEntry>, packetPr.cacheObject as HashSet<RequestEntry>)
        }
        
        @JvmStatic
        fun registerResponseListener(listener: FriendRequestResponseListener) {
            responseListener.add(listener)
        }
    
        @JvmStatic
        fun registerInviteListener(listener: FriendInviteListener) {
            requestListener.add(listener)
        }
    }
}