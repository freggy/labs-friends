package de.bergwerklabs.friends.client.bukkit

import de.bergwerklabs.commons.spigot.chat.messenger.PluginMessenger
import org.bukkit.plugin.java.JavaPlugin

internal var friendsClient: FriendsBukkitClient? = null

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsBukkitClient : JavaPlugin() {
    
    val messenger = PluginMessenger("Friends")
    
    override fun onEnable() {
        friendsClient = this
    }
}