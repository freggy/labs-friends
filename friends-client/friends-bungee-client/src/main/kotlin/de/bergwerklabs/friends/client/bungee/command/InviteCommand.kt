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
                val receiver = optional.get()
                friendsClient!!.requests[sender.uniqueId]!!.add(receiver)
                FriendsApi.sendInvite(sender.uniqueId, receiver)
                friendsClient!!.messenger.message("§7Deiner Anfrage wurde versendet.", sender)
            }
            else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt :(", sender)
        }
    }
}