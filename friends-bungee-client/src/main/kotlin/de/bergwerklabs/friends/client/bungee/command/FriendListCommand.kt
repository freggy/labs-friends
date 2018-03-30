package de.bergwerklabs.friends.client.bungee.command

import com.google.common.collect.Iterables
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.common.*
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
        if (sender !is ProxiedPlayer) return
        val page = if (args!!.isEmpty() || args[0].isEmpty()) 1 else args[0].toInt()
        
        FriendsApi.getFriendList(sender.uniqueId).thenAcceptAsync { friends ->
            if (friends.isEmpty()) {
                friendsClient!!.messenger.message(
                    "§c${funnySentences[Random().nextInt(funnySentences.size)]}", sender
                )
                return@thenAcceptAsync
            }
            
            val pairs = friends.map { friend ->  Pair(ChatColor.getByChar(getColorBlocking(friend.mapping.uuid)[1]), friend.mapping) }
            val sorted = pairs.sortedWith(kotlin.Comparator { obj1, obj2 -> compareFriends(obj1, obj2) })
            val pages = Iterables.partition(sorted, pageSize).toList()
    
            if (page > pages.size || page <= 0) {
                friendsClient!!.messenger.message("§cSeitenzahl zu groß oder zu klein.", sender)
                return@thenAcceptAsync
            }
            
            val entries = pages[page - 1]
                .map { pair ->
                    Entry(
                        pair.second.name,
                        pair.first,
                        pair.second.uuid,
                        PlayerResolver.getOnlinePlayerCacheEntry(pair.second.uuid).join()
                    )
                }
    
            sender.sendMessage(
                ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------§b Freundesliste §6§m-------")
            )
            
            list(entries, sender, true)
            
            sender.sendMessage(
                ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m----------§b [$page/${pages.size}] §6§m-----------")
            )
        }
    }
}