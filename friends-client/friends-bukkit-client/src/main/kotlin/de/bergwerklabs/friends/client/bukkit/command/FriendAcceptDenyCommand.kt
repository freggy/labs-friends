package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.friends.client.bukkit.friendsClient
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * Created by Yannic Rieger on 05.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendAcceptDenyCommand : CommandExecutor {
    
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        
        if (sender is Player) {
            val name = args!![0]
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            
            if (label.equals("accept", true)) {
                return this.process(name, sender, friendList, { acceptor, accepted ->
                    FriendsApi.respondToInvite(acceptor, accepted, FriendRequestResponse.ACCEPTED)
                })
            }
            else if (label.equals("deny", true)) {
                return this.process(name, sender, friendList, { acceptor, accepted ->
                    FriendsApi.respondToInvite(acceptor, accepted, FriendRequestResponse.DENIED)
                })
            }
        }
        return true
    }
    
    private fun process(name: String, sender: Player, friendList: Set<UUID>, func: (UUID, UUID) -> Unit): Boolean {
        val playerOnServer = Bukkit.getPlayer(name)
        
        // if the player is on the server we don't have to resolve the name to a UUID.
        if (playerOnServer != null) {
            if (!friendList.contains(playerOnServer.uniqueId)) {
                friendsClient!!.messenger.message("§cDieser Spieler hat dir keine Freundschaftsanfrage gesendet..", sender)
                return true
            }
            func.invoke(sender.uniqueId, playerOnServer.uniqueId)
        }
        else {
            val optional = PlayerResolver.resolveNameToUuid(name)
            if (optional.isPresent) {
                val uuid = optional.get()
                if (!friendList.contains(uuid)) {
                    friendsClient!!.messenger.message("§cDieser Spieler hat dir keine Freundschaftsanfrage gesendet..", sender)
                    return true
                }
                func.invoke(sender.uniqueId, uuid)
            }
            else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt.", sender)
        }
        return true
    }
}