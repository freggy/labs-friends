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
        println("1")
        if (sender is ProxiedPlayer) {
            println("2")
            val page = if (args!!.isEmpty() || args[0].isEmpty()) 1 else args[0].toInt()
    
            println("3")
            FriendsApi.getFriendList(sender.uniqueId).thenAccept { friends ->
                println("4")
                try {
                    println("5")
                    if (friends.isEmpty()) {
                        friendsClient!!.messenger.message(
                            "§c${funnySentences[Random().nextInt(funnySentences.size)]}", sender
                        )
                        return@thenAccept
                    }
    
                    println("6")
                    val pages = Iterables.partition(friends, pageSize).toList()
    
    
                    println("7")
                    if (page > pages.size || page <= 0) {
                        friendsClient!!.messenger.message("§cSeitenzahl zu groß oder zu klein.", sender)
                        return@thenAccept
                    }
    
                    println("8")
                    pages[page].forEach {
                        println("hello")
                        println(PlayerResolver.getOnlinePlayerCacheEntry(it.mapping.uuid).join())
                    }
    
                    println("9")
                    
                    val sorted = pages[page]
                        .map { friend ->
                            Entry(
                                friend.mapping.name,
                                ChatColor.getByChar(getColorBlocking(friend.mapping.uuid)[1]),
                                friend.mapping.uuid,
                                PlayerResolver.getOnlinePlayerCacheEntry(friend.mapping.uuid).join()
                            )
                        }
                        .sortedWith(kotlin.Comparator { obj1, obj2 -> compareFriends(obj1, obj2) }).toList()
    
                    println("10")
    
                    sender.sendMessage(
                        ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------§b Freundesliste §6§m-------")
                    )
                    list(sorted, sender, true)
                    println("11")
                    val size = Math.ceil(friends.size.toDouble() / pageSize).toInt()
                    println("12")
                    sender.sendMessage(
                        ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m----------§b [$page/$size] §6§m-----------")
                    )
                    println("13")
                }
                catch (ex: Exception) {
                    ex.printStackTrace()
                }
                println("14")
            }
        }
    }
}