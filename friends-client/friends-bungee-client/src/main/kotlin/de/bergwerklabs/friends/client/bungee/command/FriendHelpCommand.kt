package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 04.12.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendHelpCommand : BungeeCommand {
    
    override fun getUsage() = "/friend help"
    
    override fun getName() = "help"
    
    override fun getDescription() = "Listet alle Commands auf."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        friendsClient!!.helpDisplay.display(sender as? ProxiedPlayer ?: return, friendsClient!!.messenger)
    }
}