package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.common.prefix
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
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
            
            if (args!!.isEmpty()) {
                friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                return
            }
            
            FriendsApi.respondToInvite(
                PlayerNameToUuidMapping(sender.name, sender.uniqueId),
                PlayerNameToUuidMapping(args[0], null),
                FriendRequestResponse.ACCEPTED
            )
            
            sender.sendMessage(
                ChatMessageType.CHAT,
                *TextComponent.fromLegacyText("${prefix} Du hast die Freundschaftsanfrage §cabgelehnt.")
            )
        }
    }
}