package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
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
class FriendAcceptCommand : BungeeCommand {

    override fun getUsage() = "/friend accept <name>"

    override fun getName() = "accept"

    override fun getDescription() = "Akzeptiert eine Freundschaftsanfrage."

    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            if (args!!.isEmpty()) {
                friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                return
            }

            val name = args[0]

            FriendsApi.getPendingInvites(sender.uniqueId).thenApply { invites ->
                return@thenApply invites.any { request -> request.mapping.name.equals(name, true) }
            }
            .thenAccept { hasRequested ->
                if (!hasRequested) return@thenAccept

                FriendsApi.respondToInvite(
                    PlayerNameToUuidMapping(sender.name, sender.uniqueId),
                    PlayerNameToUuidMapping(args[0], null),
                    FriendRequestResponse.ACCEPTED
                ).thenAccept { data ->
                    when (data.response) {
                        FriendRequestResponse.FRIEND_LIST_FULL -> {
                            this.sendMessage(sender, "Deine Freundesliste ist voll.")
                        }
                        FriendRequestResponse.SUCCESS -> {
                            sendMessage(sender, "Du hast die Freundschaftsanfrage §aangenommen.")
                        }
                        FriendRequestResponse.UNKNOWN_NAME -> {
                            this.sendMessage(sender, "§cDieser Spieler ist uns nicht bekannt.")
                        }
                        else -> return@thenAccept
                    }
                }
            }
        }
    }

    private fun sendMessage(sender: ProxiedPlayer, message: String) {
        sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("${prefix} $message"))
    }
}