package de.bergwerklabs.friends.client.bukkit.command

import com.google.common.collect.Iterables
import de.bergwerklabs.api.cache.pojo.players.online.OnlinePlayerCacheEntry
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.commons.spigot.chat.ChatCommons
import de.bergwerklabs.framework.commons.spigot.command.ChildCommand
import de.bergwerklabs.friends.client.bukkit.friendsClient
import mkremins.fanciful.FancyMessage
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class FriendListCommand : ChildCommand {
    
    private val pageSize = 10
    
    override fun getName() = "list"
    
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender is Player) {
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            if (friendList.isNotEmpty()) {
                sender.sendMessage("§6§m-------§b Freundesliste §6§m-------")
                
                if (args!![0].isNullOrEmpty() || args[0].isBlank())
                    friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
    
                val page = args[0].toInt()
    
                try {
                    this.listFriends(page, friendList, sender)
                }
                catch (ex: IllegalArgumentException) {
                    return true
                }
                
                sender.sendMessage("§6§m----------§b [$page/${Math.ceil(friendList.size.toDouble() / pageSize)}] §6§m----------")
            }
        }
        return true
    }
    
    /**
     * Lists 10 friends at a time and displays them to the player.
     * The number of player displayed is defined by [pageSize] which is currently 10.
     *
     * @param page    page the player wants to navigate to.
     * @param friends set containing all the players friends.
     * @param player  player that executed the command.
     */
    private fun listFriends(page: Int, friends: Set<UUID>, player: Player) {
        val pages = Iterables.toArray(Iterables.paddedPartition(friends, pageSize), List::class.java) as Array<List<UUID?>>
        
        if (page > pages.size || page < pages.size) throw IllegalArgumentException("Index out of bounds")
        
        pages[page - 1].forEach { friendUuid ->
            if (friendUuid != null) {
                PlayerResolver.resolveUuidToName(friendUuid).ifPresent {
                    val online = PlayerResolver.getOnlinePlayerCacheEntry(friendUuid.toString())
                    val message = FancyMessage("✖").color(ChatColor.RED).command("/friend remove $it").tooltip("Entferne $it aus der Freundesliste.")
                            .then("✸").color(ChatColor.GOLD).command("/party invite $it").tooltip("Lade $it in eine Party ein")
                            .then("➥").color(ChatColor.AQUA).command("/friend jump $it").tooltip("Joine $it nach")
                            .then(" $it").color(ChatCommons.chatColorFromColorCode(friendsClient!!.zPermissionService.getPlayerPrefix(player.uniqueId)).get())
                            .then(" - ").color(ChatColor.DARK_GRAY)
                    
                    if (online.isPresent) {
                        val currentServer = online.get().currentServer
                        // TODO: generate display name
                        message.then(currentServer.service).color(ChatColor.GRAY)
                                                           .tooltip("Id: ${currentServer.containerId}")
                    }
                    else message.then("OFFLINE").color(ChatColor.RED)
                    message.send(player)
                }
            }
        }
    }
}