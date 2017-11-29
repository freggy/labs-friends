package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.client.bukkit.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class InviteCommand : BungeeCommand {
    
    override fun getName() = "invite"
    
    override fun getDescription(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun getUsage(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val label = args!![0]
            val optional = PlayerResolver.resolveNameToUuid(label)
            if (optional.isPresent) {
                FriendsApi.sendInvite(sender.uniqueId, optional.get())
            }
            else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt :(", sender)
        }
    }
}