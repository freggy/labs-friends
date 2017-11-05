package de.bergwerklabs.friends.client.bukkit

import de.bergwerklabs.commons.spigot.chat.ChatCommons
import de.bergwerklabs.commons.spigot.chat.messenger.PluginMessenger
import de.bergwerklabs.friends.client.bukkit.command.FriendAcceptDenyCommand
import de.bergwerklabs.friends.client.bukkit.command.FriendParentCommand
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService
import java.util.*

internal var friendsClient: FriendsBukkitClient? = null

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendsBukkitClient : JavaPlugin(), Listener {
    
    val messenger = PluginMessenger("Friends")
    lateinit var zPermissionService: ZPermissionsService
    
    override fun onEnable() {
        friendsClient = this
        this.server.pluginManager.registerEvents(this, this)
        zPermissionService = this.server.servicesManager.load(ZPermissionsService::class.java)
        // TODO: add child commands
        this.getCommand("friend").executor = FriendParentCommand("friend", FriendAcceptDenyCommand())
        FriendsApi.registerFriendResponseListener(RequestResponseListener())
        FriendsApi.registerFriendRequestListener(FriendRequestListener())
    }
    
    fun getRankColor(uuid: UUID): ChatColor {
        val optional = ChatCommons.chatColorFromColorCode(friendsClient!!.zPermissionService.getPlayerPrefix(uuid))
        return if (optional.isPresent) optional.get() else ChatColor.BOLD
    }
}