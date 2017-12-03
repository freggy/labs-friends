package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.framework.commons.bungee.chat.PluginMessenger
import de.bergwerklabs.framework.commons.bungee.permissions.ZBridge
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.command.*
import de.bergwerklabs.friends.client.bungee.common.getLoginMessage
import de.bergwerklabs.friends.client.bungee.common.getLogoutMessage
import de.bergwerklabs.friends.client.bungee.common.sendMessageToFriends
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

internal var friendsClient: FriendsBungeeClient? = null

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsBungeeClient : Plugin(), Listener {
    
    val messenger = PluginMessenger("Friends")
    val zBridge = ZBridge()
    val requests = HashMap<UUID, MutableSet<UUID>>()
    private val service = AtlantisPackageService(FriendLoginPacket::class.java)
    
    override fun onEnable() {
        friendsClient = this
        // TODO: add child commands
        this.proxy.pluginManager.registerCommand(this, FriendParentCommand(
                "friend",
                "",
                "",
                null,
                FriendListCommand(),
                InviteCommand(),
                FriendDenyCommand(),
                FriendAcceptCommand(),
                FriendRemoveCommand(),
                FriendListInvitesCommand()))
        
        this.proxy.pluginManager.registerListener(this, this)
        FriendsApi.registerResponseListener(RequestResponseListener())
        FriendsApi.registerInviteListener(FriendRequestListener())
        
        this.service.addListener(FriendLoginPacket::class.java, { packet ->
            val receiver = this.proxy.getPlayer(packet.messageReceiver)
            receiver?.let {
                friendsClient!!.messenger.message(TextComponent.toLegacyText(*getLoginMessage(packet.onlinePlayer.name, this.zBridge.getRankColor(packet.onlinePlayer.uuid))), receiver)
            }
        })
        
        this.service.addListener(FriendLoginPacket::class.java, { packet ->
            val receiver = this.proxy.getPlayer(packet.messageReceiver)
            receiver?.let {
                friendsClient!!.messenger.message(TextComponent.toLegacyText(*getLogoutMessage(packet.onlinePlayer.name, this.zBridge.getRankColor(packet.onlinePlayer.uuid))), receiver)
            }
        })
    }
    
    @EventHandler
    fun onPlayerLogin(event: PostLoginEvent) {
        val player = event.player
        val info = FriendsApi.retrieveFriendInfo(player.uniqueId)
        requests.putIfAbsent(player.uniqueId, HashSet())
        
        info.pendingInvites.forEach { inv ->
            friendsClient!!.messenger.message("Acc ${inv.acceptor}", player)
            friendsClient!!.messenger.message("Req ${inv.requester}", player)
        }
        
        if (info.pendingInvites.isNotEmpty())  {
            if (info.pendingInvites.size == 1)
                friendsClient!!.messenger.message("Du hast §beine §7ausstehende Anfrage.", player)
            else
                friendsClient!!.messenger.message("Du hast §b${info.pendingInvites.size} §7ausstehende Anfragen.", player)
        }
        sendMessageToFriends(info.friendList, this.service, this.proxy, player, true)
    }
    
    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player
        requests.remove(player.uniqueId)
        sendMessageToFriends(FriendsApi.getFriendlist(player.uniqueId), this.service, this.proxy, player, false)
    }
    
    internal fun process(name: String, sender: ProxiedPlayer, friendList: Set<FriendEntry>, func: (UUID, UUID) -> Unit) {
        val playerOnServer = this.proxy.getPlayer(name)
        // TODO: refactor
        
        // if the player is on the server we don't have to resolve the name to a UUID.
        if (playerOnServer != null) {
            if (friendList.any { entry -> entry.friend == playerOnServer.uniqueId }) {
                friendsClient!!.messenger.message("§cDieser Spieler ist bereits in deiner Freundesliste", sender)
                return
            }
            else if (!this.requests[playerOnServer.uniqueId]!!.contains(sender.uniqueId)) {
                friendsClient!!.messenger.message("§cDieser Spieler hat dir keine Anfrage gesendet.", sender)
                return
            }
            this.requests[playerOnServer.uniqueId]!!.remove(sender.uniqueId)
            func.invoke(sender.uniqueId, playerOnServer.uniqueId)
        }
        else {
            val optional = PlayerResolver.resolveNameToUuid(name)
            if (optional.isPresent) {
                val uuid = optional.get()
                if (friendList.any { entry -> entry.friend == uuid }) {
                    friendsClient!!.messenger.message("§cDieser Spieler ist bereits in deiner Freundesliste", sender)
                    return
                }
                else if (!this.requests[uuid]!!.contains(sender.uniqueId)) {
                    friendsClient!!.messenger.message("§cDieser Spieler hat dir keine Anfrage gesendet.", sender)
                    return
                }
                this.requests[uuid]!!.remove(sender.uniqueId)
                func.invoke(sender.uniqueId, uuid)
            }
            else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt.", sender)
        }
    }
}