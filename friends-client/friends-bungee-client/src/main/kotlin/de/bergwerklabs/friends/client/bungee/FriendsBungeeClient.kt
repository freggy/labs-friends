package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.framework.commons.bungee.chat.PluginMessenger
import de.bergwerklabs.framework.commons.bungee.permissions.ZBridge
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.command.*
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.*
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
                FriendAcceptCommand()))
        
        this.proxy.pluginManager.registerListener(this, this)
        FriendsApi.registerResponseListener(RequestResponseListener())
        FriendsApi.registerInviteListener(FriendRequestListener())
        
        this.service.addListener(FriendLoginPacket::class.java, { packet ->
            val receiver = this.proxy.getPlayer(packet.messageReceiver)
            receiver?.sendMessage(ChatMessageType.CHAT, *this.getLoginMessage(packet.onlinePlayer, this.zBridge.getRankColor(receiver.uniqueId)))
        })
        
    }
    
    @EventHandler
    fun onPlayerLogin(event: PostLoginEvent) {
        val player = event.player
        val info = FriendsApi.retrieveFriendInfo(player.uniqueId)
        requests.putIfAbsent(player.uniqueId, HashSet())
        
        if (info.pendingInvites.isNotEmpty())  {
            friendsClient!!.messenger.message("Du hast ${info.pendingInvites.size} ausstehende Anfragen.", player)
        }
        
        info.friendList.forEach { entry ->
            val playerOnServer = this.proxy.getPlayer(entry.friend)
            
            if (playerOnServer != null) {
                playerOnServer.sendMessage(ChatMessageType.CHAT, *this.getLoginMessage(playerOnServer.name, this.zBridge.getRankColor(playerOnServer.uniqueId)))
            }
            else {
                this.service.sendPackage(FriendLoginPacket(player.name, entry.friend))
            }
        }
    }
    
    fun getLoginMessage(name: String, color: ChatColor): Array<BaseComponent> {
        return ComponentBuilder(name).color(color)
                .append(" ist ").color(ChatColor.GRAY)
                .append("online").color(ChatColor.GREEN)
                .append("[").color(ChatColor.GRAY)
                .append("EINLADEN").color(ChatColor.GOLD)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party invite $name"))
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("$name zu einer party einladen.")))
                .create()
    }
    
    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        requests.remove(event.player.uniqueId)
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