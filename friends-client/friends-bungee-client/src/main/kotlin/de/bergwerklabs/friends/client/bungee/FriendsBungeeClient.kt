package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.chat.PluginMessenger
import de.bergwerklabs.framework.commons.bungee.permissions.ZBridge
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.command.FriendListCommand
import de.bergwerklabs.friends.client.bungee.command.FriendParentCommand
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import java.util.*

internal var friendsClient: FriendsBungeeClient? = null

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsBungeeClient : Plugin(), Listener {
    
    val messenger = PluginMessenger("Friends")
    val zBridge = ZBridge("forumpd", "fceAVWB5LNdt6aSD")
    
    override fun onEnable() {
        friendsClient = this
        // TODO: add child commands
        this.proxy.pluginManager.registerCommand(this, FriendParentCommand("friend", "", "", null, FriendListCommand()))
        FriendsApi.registerResponseListener(RequestResponseListener())
        FriendsApi.registerInviteListener(FriendRequestListener())
    }
    
    internal fun process(name: String, sender: ProxiedPlayer, friendList: Set<FriendEntry>, func: (UUID, UUID) -> Unit): Boolean {
        val playerOnServer = this.proxy.getPlayer(name)
        
        // if the player is on the server we don't have to resolve the name to a UUID.
        if (playerOnServer != null) {
            if (!friendList.any { entry -> entry.friend == playerOnServer.uniqueId }) {
                friendsClient!!.messenger.message("§cDieser Spieler hat dir keine Freundschaftsanfrage gesendet..", sender)
                return true
            }
            func.invoke(sender.uniqueId, playerOnServer.uniqueId)
        }
        else {
            val optional = PlayerResolver.resolveNameToUuid(name)
            if (optional.isPresent) {
                val uuid = optional.get()
                if (!friendList.any { entry -> entry.friend == uuid }) {
                    friendsClient!!.messenger.message("§cDieser Spieler hat dir keine Freundschaftsanfrage gesendet..", sender)
                    return true
                }
                func.invoke(sender.uniqueId, uuid)
            }
            else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt.", sender)
        }
        return true
    }
}