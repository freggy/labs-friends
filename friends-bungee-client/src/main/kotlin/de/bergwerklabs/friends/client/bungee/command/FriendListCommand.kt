package de.bergwerklabs.friends.client.bungee.command

import com.google.common.collect.Iterables
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.common.Entry
import de.bergwerklabs.friends.client.bungee.common.compareFriends
import de.bergwerklabs.friends.client.bungee.common.getColorBlocking
import de.bergwerklabs.friends.client.bungee.common.list
import de.bergwerklabs.friends.client.bungee.friendsClient
import me.lucko.luckperms.LuckPerms
import me.lucko.luckperms.api.Group
import me.lucko.luckperms.api.User
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class FriendListCommand : BungeeCommand {
    
    private val pageSize = 8
    
    override fun getName() = "list"
    
    override fun getDescription() = "Listet alle deine Freunde auf."
    
    override fun getUsage() = "/friend list"
    
    private val funnySentences = arrayOf(
            "Keine Freunde. Schade.",
            "Du hast leider keine Freunde. Wenn du dich danach besser fühlst: Ist nur ein Datenbank-Problem",
            "Wir konnten leider keine Freunde finden :(",
            "Du hast noch keine Freunde."
    )
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            FriendsApi.getFriendList(sender.uniqueId)
                .thenApplyAsync({ friends ->
                    friends.map { friend ->
                        Entry(friend.mapping.name, ChatColor.getByChar(getColorBlocking(friend.mapping.uuid)[1]))
                    }
                })
                .thenAccept { friends ->
                    if (friends.isEmpty()) {
                        friendsClient!!.messenger.message("§c${funnySentences[Random().nextInt(funnySentences.size)]}", sender)
                        return@thenAccept
                    }
                    
                    if (args!![0].isEmpty() || args[0].isBlank()) {
                        friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                        return@thenAccept
                    }
                    
                    val page = if (args[0].isEmpty()) 1 else args[0].toInt()
                    val sorted = friends.sortedWith(kotlin.Comparator { obj1, obj2 -> compareFriends(obj1, obj2)  }).toList()
                    
                    val pages = Iterables.partition(sorted, pageSize).toList()
                    
                    if (page > pages.size || page <= 0) {
                        friendsClient!!.messenger.message("§cSeitenzahl zu groß oder zu klein.", sender)
                        return@thenAccept
                    }
                    
                    sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------§b Freundesliste §6§m-------"))
                    list(page, pages, sender, true)
                    sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m----------§b [$page/${(Math.ceil(friends.size.toDouble() / pageSize)).toInt()}] §6§m-----------"))
            }
        }
    }
}