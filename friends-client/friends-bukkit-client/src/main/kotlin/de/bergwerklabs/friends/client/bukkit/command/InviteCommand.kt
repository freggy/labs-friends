package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.framework.commons.spigot.command.ChildCommand
import de.bergwerklabs.friends.client.bukkit.friendsClient
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class InviteCommand : ChildCommand{
    
    override fun getName() = "invite"
    
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender is Player) {
            val optional = PlayerResolver.resolveNameToUuid(label)
            if (optional.isPresent) {
                FriendsApi.sendInvite(sender.uniqueId, optional.get())
            }
            else friendsClient!!.messenger.message("§cDieser Spieler ist uns nicht bekannt :(", sender)
        }
        return true
    }
    
}