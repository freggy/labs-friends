package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 29.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendDenyCommand : BungeeCommand {
    
    override fun getUsage() = "/friend deny <name>"
    
    override fun getName() = "deny"
    
    override fun getDescription() = "Lehnt eine Freundschaftsanfrage ab."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
    
            // Run async because friendsClient#process is blocking
            friendsClient!!.runAsync {
                if (args!!.isEmpty()) {
                    friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                    return@runAsync
                }
    
                friendsClient!!.process(args[0], sender, { denier, denied ->
                    FriendsApi.respondToInvite(denier, denied, FriendRequestResponse.DENIED)
                    friendsClient!!.messenger.message("§bDu hast die Anfrage §cabgelehnt", sender)
                })
            }
        }
    }
}