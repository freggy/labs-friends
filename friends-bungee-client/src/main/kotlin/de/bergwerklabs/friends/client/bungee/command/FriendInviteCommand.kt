package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.common.getRankColor
import de.bergwerklabs.friends.client.bungee.common.prefix
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class FriendInviteCommand : BungeeCommand {
    
    override fun getName() = "add"
    
    override fun getDescription() = "Schickt einem Spieler eine Freundschaftsanfrage."
    
    override fun getUsage() = "/friend add <spieler>"
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val label = args!![0]
            if (label.equals(sender.name, true)) return
            val future = FriendsApi.sendInvite(PlayerNameToUuidMapping(sender.name, sender.uniqueId), PlayerNameToUuidMapping(label, null))
            
            future.thenAccept { data ->
                when (data.response) {
                    FriendRequestResponse.ALREADY_FRIENDS -> {
                        this.sendMessage(sender, "Du bist bereits mit diesem Spieler befreundet.")
                    }
                    FriendRequestResponse.FRIEND_LIST_FULL -> {
                        this.sendMessage(sender, "Deine Freundesliste ist voll.")
                    }
                    FriendRequestResponse.ALREADY_REQUESTED -> {
                        this.sendMessage(sender, "Du hast diesen Spieler bereits eine Anfrage geschickt.")
                    }
                    FriendRequestResponse.SUCCESS -> {
                        this.sendMessage(sender, "${getRankColor(data.receiver.uuid)}${data.receiver.name} §7hat deine Anfrage §aangenommen§7.")
                    }
                    else -> return@thenAccept
                }
            }
            friendsClient!!.messenger.message("§7Deine Anfrage wurde versendet.", sender)
        }
    }
    
    private fun sendMessage(sender: ProxiedPlayer, message: String) {
        sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("$prefix$message"))
    }
}