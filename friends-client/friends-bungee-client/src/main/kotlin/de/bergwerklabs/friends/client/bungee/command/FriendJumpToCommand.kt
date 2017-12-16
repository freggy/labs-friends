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
        if (sender is ProxiedPlayer) {
            val to = args!![0]
            
            if (args.isEmpty()) {
                friendsClient!!.messenger.message("§cDu musst einen Namen angeben.", sender)
                return
            }
            
            // PlayerResolver#getOnlinePlayerCacheEntry is blocking
            friendsClient!!.runAsync {
                val nameOptional = PlayerResolver.resolveNameToUuid(to)
                if (nameOptional.isPresent) {
                    if (!FriendsApi.getFriendlist(sender.uniqueId).any { entry -> entry.friend == nameOptional.get() }) {
                        friendsClient!!.messenger.message("§cDieser Spieler ist nicht in deiner Freundesliste.", sender)
                        return@runAsync
                    }
                }
                
                val optional = PlayerResolver.getOnlinePlayerCacheEntry(to)
                if (optional.isPresent) {
                    val info = optional.get().currentServer ?: return@runAsync
                    friendsClient!!.proxy.getServerInfo("${info.containerId}_${info.service}")?.let {
                        sender.connect(it)
                    }
                }
                else friendsClient!!.messenger.message("§cDieser Spieler ist zur Zeit nicht online.", sender)
            }
        }
    }
}