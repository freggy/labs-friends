package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.client.bukkit.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
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
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            friendsClient!!.process(name, sender, friendList, { acceptor, accepted ->
                FriendsApi.respondToInvite(acceptor, accepted, FriendRequestResponse.DENIED)
            })
        }
    }
}