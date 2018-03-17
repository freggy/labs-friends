package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.api.friends.FriendLogoutPacket
import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.atlantis.api.friends.FriendServerInviteRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.framework.commons.bungee.chat.PluginMessenger
import de.bergwerklabs.framework.commons.bungee.command.help.CommandHelpDisplay
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.command.*
import de.bergwerklabs.friends.client.bungee.common.*
import de.bergwerklabs.permissionbridge.luckperms.common.LuckPermsBridge
import me.lucko.luckperms.LuckPerms
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler

internal var friendsClient: FriendsBungeeClient? = null

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsBungeeClient : Plugin(), Listener {
    
    val messenger = PluginMessenger("Friends")
    lateinit var helpDisplay: CommandHelpDisplay
    
    private val requestListener = FriendRequestListener()
    
    override fun onEnable() {
        friendsClient = this
        bridge = LuckPermsBridge(LuckPerms.getApi())
        val helpCommand = FriendHelpCommand()
        
        packageService.addListener(FriendServerInviteRequestPacket::class.java, { packet ->
            this.requestListener.onInvite(packet.sender, packet.receiver)
        })
        
        packageService.addListener(FriendInviteServerResponse::class.java, { packet ->
            this.proxy.getPlayer(packet.receiver.uuid)?.let { player ->
                val text = if (packet.response == FriendRequestResponse.ACCEPTED) {
                    "§aangenommen§7."
                }
                else "§cabgelehnt§7."
                player.sendMessage(
                    ChatMessageType.CHAT,
                    *TextComponent.fromLegacyText(
                        "$prefix${getRankColor(packet.sender.uuid)}${packet.sender.name} §7hat deine Anfrage $text"
                    )
                )
            }
        })
        
        packageService.addListener(FriendLoginPacket::class.java, { packet ->
            this.proxy.getPlayer(packet.messageReceiver.uuid)?.sendMessage(
                ChatMessageType.CHAT,
                *getLoginMessage(packet.onlinePlayer.name, getRankColor(packet.onlinePlayer.uuid))
            )
        })
        
        packageService.addListener(FriendLogoutPacket::class.java, { packet ->
            this.proxy.getPlayer(packet.messageReceiver.uuid)?.sendMessage(
                ChatMessageType.CHAT,
                *getLogoutMessage(packet.onlinePlayer.name, getRankColor(packet.onlinePlayer.uuid))
            )
        })
        
        val parent = FriendParentCommand(
            "friend",
            "",
            "",
            helpCommand,
            FriendListCommand(),
            FriendInviteCommand(),
            FriendDenyCommand(),
            FriendAcceptCommand(),
            FriendRemoveCommand(),
            FriendListInvitesCommand(),
            FriendJumpToCommand(),
            helpCommand
        )
        
        this.proxy.pluginManager.registerCommand(this, parent)
        this.helpDisplay = CommandHelpDisplay(parent.subCommands.toSet())
        this.proxy.pluginManager.registerListener(this, this)
    }
    
    @EventHandler
    fun onPlayerLogin(event: PostLoginEvent) {
        val player = event.player
        FriendsApi.sendPlayerLogin(player.uniqueId)
        FriendsApi.getPendingInvites(player.uniqueId).thenAccept { requests ->
            val size = requests.size
            if (size == 1) {
                player.sendMessage(
                    ChatMessageType.CHAT, *TextComponent.fromLegacyText("${prefix}Du hast §beine §7Anfrage.")
                )
            }
            else if (size >= 2) {
                player.sendMessage(
                    ChatMessageType.CHAT, *TextComponent.fromLegacyText("${prefix}Du hast §b$size §7Anfragen.")
                )
            }
        }
        sendMessageToFriends(player, true)
    }
    
    @EventHandler
    fun onPlayerDisconnect(event: PlayerDisconnectEvent) {
        val player = event.player
        FriendsApi.sendPlayerLogout(player.uniqueId)
        sendMessageToFriends(player, false)
    }
}