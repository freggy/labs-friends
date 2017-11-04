package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.commons.spigot.chat.ChatCommons
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
class FriendRemoveCommand : ChildCommand {
    
    override fun getName() = "remove"
    
    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        if (sender is Player) {
            PlayerResolver.resolveNameToUuid(label).ifPresent {
                if (FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList.contains(it)) {
                    FriendsApi.removeFriend(sender.uniqueId, it)
                    val rankColor = ChatCommons.chatColorFromColorCode(friendsClient!!.zPermissionService.getPlayerPrefix(it)).get()
                    friendsClient!!.messenger.message("$rankColor$label§r wurde aus deiner Freundesliste entfernt.", sender)
                }
                else friendsClient!!.messenger.message("§cDieser Spieler ist nicht mit dir befreundet.", sender)
            }
        }
        return true
    }
}