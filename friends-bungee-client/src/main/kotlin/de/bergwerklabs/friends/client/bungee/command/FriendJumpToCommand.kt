package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
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
        if (sender !is ProxiedPlayer) return
        
        val jumpTo = args!![0]
        
        if (jumpTo.isEmpty()) {
            friendsClient!!.messenger.message("§cDu musst einen Namen angeben.", sender)
            return
        }
        else if (jumpTo.equals(sender.name, true)) {
            friendsClient!!.messenger.message("§4FATAL ERROR", sender)
            return
        }
        
        PlayerResolver.resolveNameToUuidAsync(jumpTo).thenAccept { optional ->
            optional.ifPresent { mapping ->
                PlayerResolver.getOnlinePlayerCacheEntry(mapping.uuid).thenAccept { optional ->
                    if (optional.isPresent) {
                        val current = optional.get()
                        sender.connect(friendsClient!!.proxy.getServerInfo("${current.server.id}_${current.server.service}"))
                    }
                    else friendsClient!!.messenger.message("§cDieser Spieler ist nicht online.", sender)
                }
            }
        }
    }
}