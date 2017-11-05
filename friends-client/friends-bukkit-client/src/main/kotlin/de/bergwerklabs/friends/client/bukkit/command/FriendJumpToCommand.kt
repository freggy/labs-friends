package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.spigot.command.ChildCommand
import de.bergwerklabs.framework.commons.spigot.pluginmessage.PluginMessageOption
import de.bergwerklabs.framework.commons.spigot.pluginmessage.PluginMessages
import de.bergwerklabs.friends.client.bukkit.friendsClient
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendJumpToCommand : ChildCommand {
    
    override fun getName() = "tp"
    
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender is Player) {
            val to = args!![0]
            PlayerResolver.getOnlinePlayerCacheEntry(to).ifPresent {
                PluginMessages.sendPluginMessage(friendsClient, PluginMessageOption.CONNECT_OTHER, sender.displayName, it.currentServer.containerId)
            }
        }
        return true
    }
}