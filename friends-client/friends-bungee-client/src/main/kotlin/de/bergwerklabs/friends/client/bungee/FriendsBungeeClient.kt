package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.atlantis.api.corepackages.AtlantisCache
import de.bergwerklabs.atlantis.api.corepackages.cache.CacheUnloadPacket
import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.api.friends.FriendLogoutPacket
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.framework.commons.bungee.chat.PluginMessenger
import de.bergwerklabs.framework.commons.bungee.command.help.CommandHelpDisplay
import de.bergwerklabs.framework.commons.bungee.permissions.ZBridge
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.command.*
import de.bergwerklabs.friends.client.bungee.common.PlayerSettingsWrapper
import de.bergwerklabs.friends.client.bungee.common.getLoginMessage
import de.bergwerklabs.friends.client.bungee.common.getLogoutMessage
import de.bergwerklabs.friends.client.bungee.common.sendMessageToFriends
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler
import java.util.*

internal var friendsClient: FriendsBungeeClient? = null

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsBungeeClient : Plugin(), Listener {
    
    val messenger = PluginMessenger("Friends")
    val zBridge = ZBridge()
    val settings = PlayerSettingsWrapper()
    lateinit var helpDisplay: CommandHelpDisplay
    private val service = AtlantisPackageService(FriendLoginPacket::class.java)
    
    override fun onEnable() {
        friendsClient = this
        
        val helpCommand = FriendHelpCommand()
        
        val parent = FriendParentCommand(
                "friend",
                "",
                "",
                helpCommand,
                FriendListCommand(),
                InviteCommand(),
                FriendDenyCommand(),
                FriendAcceptCommand(),
                FriendRemoveCommand(),
                FriendListInvitesCommand(),
                FriendJumpToCommand(),
                helpCommand)
        
        this.proxy.pluginManager.registerCommand(this, parent)
        this.helpDisplay = CommandHelpDisplay(parent.subCommands.toSet())
        
        this.proxy.pluginManager.registerListener(this, this)
        FriendsApi.registerResponseListener(RequestResponseListener())
        FriendsApi.registerInviteListener(FriendRequestListener())
        
        this.service.addListener(FriendLoginPacket::class.java, { packet ->
            this.runAsync {
                val receiver = this.proxy.getPlayer(packet.messageReceiver)
                receiver?.let {
                    receiver.sendMessage(net.md_5.bungee.api.ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6>> §eFriends §6❘"), *getLoginMessage(packet.onlinePlayer.name, this.zBridge.getRankColor(packet.onlinePlayer.uuid)))
                }
            }
        })
        
        this.service.addListener(FriendLogoutPacket::class.java, { packet ->
            this.runAsync {
                val receiver = this.proxy.getPlayer(packet.messageReceiver)
                receiver?.let {
                    friendsClient!!.messenger.message(TextComponent.toLegacyText(*getLogoutMessage(packet.onlinePlayer.name, this.zBridge.getRankColor(packet.onlinePlayer.uuid))), receiver)
                }
            }
        })
    }
    
    @EventHandler
    fun onPlayerLogin(event: PostLoginEvent) {
        this.runAsync {
            val player = event.player
            val info = FriendsApi.retrieveFriendInfo(player.uniqueId)
            sendMessageToFriends(info.friendList, this.service, this.proxy, player, true)
            
            if (info.pendingInvites.isNotEmpty()) {
                if (info.pendingInvites.size == 1)
                    friendsClient!!.messenger.message("Du hast §beine §7ausstehende Anfrage.", player)
                else
                    friendsClient!!.messenger.message("Du hast §b${info.pendingInvites.size} §7ausstehende Anfragen.", player)
            }
            
            this.service.sendPackage(CacheUnloadPacket<UUID>(player.uniqueId.toString(), AtlantisCache.FRIEND_LIST_CACHE))
            this.service.sendPackage(CacheUnloadPacket<UUID>(player.uniqueId.toString(), AtlantisCache.PENDING_FRIEND_REQUESTS_CACHE))
        }
    }
    
    
    fun runAsync(method: (Unit) -> Unit) {
        this.proxy.scheduler.runAsync(this, {
            method.invoke(Unit)
        })
    }
    
    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player
        
        /*
        // TODO: fix
        this.runAsync {
            sendMessageToFriends(FriendsApi.getFriendlist(player.uniqueId), this.service, this.proxy, player, false)
        } */
    }
    
    internal fun process(name: String, sender: ProxiedPlayer, func: (UUID, UUID) -> Unit) {
        val uuidOptional = PlayerResolver.resolveNameToUuid(name)
    
        if (!uuidOptional.isPresent) {
            friendsClient!!.messenger.message("§cEin schwerwiegender Fehler ist aufgetreten.", sender)
            return
        }
        
        val uuid = uuidOptional.get()
        val hasRequested = FriendsApi.getPendingRequests(sender.uniqueId).any { entry -> entry.requester == uuid }
    
        if (!hasRequested) {
            friendsClient!!.messenger.message("§cDieser Spieler hat dir keine anfrage gesendet.", sender)
            return
        }
    
        val friendList = FriendsApi.getFriendlist(sender.uniqueId)
        
        if (friendList.any { entry -> entry.friend == uuid }) {
            friendsClient!!.messenger.message("§cDieser Spieler ist bereits in deiner Freundesliste", sender)
            return
        }
        func.invoke(sender.uniqueId, uuid)
    }
}