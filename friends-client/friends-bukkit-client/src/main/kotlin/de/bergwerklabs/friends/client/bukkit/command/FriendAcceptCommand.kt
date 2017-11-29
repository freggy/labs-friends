package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.client.bukkit.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Created by Yannic Rieger on 29.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendAcceptCommand : BungeeCommand {
    
    override fun getUsage() = "/friend accept <name>"
    
    override fun getName() = "accept"
    
    override fun getDescription() = "Akzeptiert eine Freundschaftsanfrage."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            friendsClient!!.process(name, sender, friendList, { acceptor, accepted ->
                FriendsApi.respondToInvite(acceptor, accepted, FriendRequestResponse.ACCEPTED)
            })
        }
    }
}