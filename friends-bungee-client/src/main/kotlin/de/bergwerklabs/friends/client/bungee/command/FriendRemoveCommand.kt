package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
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
    
    override fun getDescription() = "Entfernt einen Spieler aus der Freundesliste."
    
    override fun getUsage() = "/friend remove <name>"
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        
        if (sender is ProxiedPlayer) {
            val name = args!![0]
            if (name.equals(sender.name, true)) return
            FriendsApi.removeFriend(
                PlayerNameToUuidMapping(name, null), PlayerNameToUuidMapping(sender.name, sender.uniqueId)
            )
            friendsClient!!.messenger.message("Der Spieler wurde aus deiner Freundesliste entfernt.", sender)
        }
    }
}