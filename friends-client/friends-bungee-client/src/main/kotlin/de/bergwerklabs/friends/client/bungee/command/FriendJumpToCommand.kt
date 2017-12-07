package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendJumpToCommand : BungeeCommand {
    
    override fun getDescription() = "Teleportiert dich zu einem anderen Spieler."
    
    override fun getUsage() = "/friend tp <name>"
    
    override fun getName() = "tp"
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val to = args!![0]
            
            // PlayerResolver#getOnlinePlayerCacheEntry is blocking
            friendsClient!!.runAsync {
                PlayerResolver.getOnlinePlayerCacheEntry(to).ifPresent {
                    val info = it.currentServer
                    sender.connect(friendsClient!!.proxy.getServerInfo("${info.service}_${info.containerId}"))
                }
            }
        }
    }
}