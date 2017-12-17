package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*
import javax.sound.midi.Receiver

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class InviteCommand : BungeeCommand {
    
    override fun getName() = "add"
    
    override fun getDescription() = "Schickt einem Spieler eine Freundschaftsanfrage."
    
    override fun getUsage() = "/friend add <spieler>"
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val label = args!![0]
            if (label.equals(sender.name, true)) return
            
            // PlayerResolver#resolveNameToUuid blocks
            friendsClient!!.runAsync {
                val optional = PlayerResolver.resolveNameToUuid(label)
                if (optional.isPresent) {
                    val receiver     = optional.get()
                    val receiverRank = friendsClient!!.zBridge.getRankInfo(receiver).group.groupId
                    val senderRank   = friendsClient!!.zBridge.getRankInfo(sender.uniqueId).group.groupId
                    
                    if (!canSendInvite(receiver, senderRank, receiverRank)) {
                        friendsClient!!.messenger.message("§cDu kannst diesem Spieler keine Freundschaftsanfrage schicken.", sender)
                        return@runAsync
                    }
                    
                    FriendsApi.sendInvite(sender.uniqueId, receiver)
                    friendsClient!!.messenger.message("§7Deine Anfrage wurde versendet.", sender)
                }
                else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt :(", sender)
            }
        }
    }
    
    /**
     *
     */
    private fun canSendInvite(receiver: UUID, senderRank: Int, receiverRank: Int): Boolean {
        return if (!friendsClient!!.settings.canReceiveInvites(receiver)) false
        else !(senderRank <= 2 && receiverRank >= 3) // Premium and default players can't send friend invites to team members and Youtubers
    }
    
}