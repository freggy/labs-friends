package de.bergwerklabs.friends.client.bukkit.command

import com.google.common.collect.Iterables
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.spigot.command.ChildCommand
import de.bergwerklabs.friends.client.bukkit.friendsClient
import mkremins.fanciful.FancyMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendListCommand : ChildCommand {
    
    private val pageSize = 10
    
    override fun getName() = "list"
    
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender is Player) {
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            if (friendList.isNotEmpty()) {
                // TODO: header
                
                if (args!![0].isNullOrEmpty() || args[0].isBlank())
                    friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                
                this.listFriends(args[0].toInt(), friendList, sender)
                // TODO: footer
            }
        }
        return true
    }
    
    
    private fun listFriends(page: Int, friends: Set<UUID>, player: Player) {
        val pages = Iterables.toArray(Iterables.paddedPartition(friends, pageSize), List::class.java) as Array<List<UUID?>>
        
        if (page > pages.size) {
            // TODO: error
            return
        }
        
        pages[page - 1].forEach { friendUuid ->
            if (friendUuid != null) {
                PlayerResolver.resolveUuidToName(friendUuid).ifPresent {
                    val message = FancyMessage("")
                }
            }
        }
    }
}