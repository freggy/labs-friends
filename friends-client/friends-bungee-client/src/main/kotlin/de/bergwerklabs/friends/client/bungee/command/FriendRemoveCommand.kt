package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class FriendRemoveCommand : BungeeCommand {
    
    override fun getName() = "remove"
    
    override fun getDescription() = ""
    
    override fun getUsage() = ""
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        val name = args!![0]
        if (sender is ProxiedPlayer) {
            
            // PlayerResolver#resolveNameToUuid blocks
            friendsClient!!.runAsync {
                PlayerResolver.resolveNameToUuid(name).ifPresent {
                    if (FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList.any { entry -> entry.friend == it }) {
                        FriendsApi.removeFriend(sender.uniqueId, it)
                        friendsClient!!.messenger.message("${friendsClient!!.zBridge.getRankColor(it)}$name§r wurde aus deiner Freundesliste entfernt.", sender)
                    }
                    else friendsClient!!.messenger.message("§cDieser Spieler ist nicht mit dir befreundet.", sender)
                }
            }
        }
    }
}