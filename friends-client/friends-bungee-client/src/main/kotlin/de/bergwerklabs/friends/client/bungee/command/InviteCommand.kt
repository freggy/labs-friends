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
    
    override fun getName() = "add"
    
    override fun getDescription() = "Schickt einem Spieler eine Freundschaftsanfrage."
    
    override fun getUsage() = "/friend invite <spieler>"
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val label = args!![0]
            if (label.equals(sender.name, true)) return
            
            // PlayerResolver#resolveNameToUuid blocks
            friendsClient!!.runAsync {
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
}