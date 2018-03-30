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

/**
 * Created by Yannic Rieger on 03.12.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendListInvitesCommand : BungeeCommand {
    
    private val pageSize = 5
    
    override fun getUsage() = "/friend invites"
    
    override fun getName() = "invites"
    
    override fun getDescription() = "Listet alle Freundschaftsanfragen auf."
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender !is ProxiedPlayer) return
    
        val page = if (args!!.isEmpty() || args[0].isEmpty()) 1 else args[0].toInt()
    
        FriendsApi.getPendingInvites(sender.uniqueId).thenAcceptAsync { invites ->
            if (invites.isEmpty()) {
                friendsClient!!.messenger.message("§cDu hast keine offenen Anfragen.", sender)
                return@thenAcceptAsync
            }
    
            val pairs = invites.map { requester ->
                Pair(
                    ChatColor.getByChar(getColorBlocking(requester.mapping.uuid)[1]), requester.mapping
                )
            }
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
                ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------§b Anfragen §6§m--------")
            )
    
            list(entries, sender, true)
    
            sender.sendMessage(
                ChatMessageType.CHAT,
                *TextComponent.fromLegacyText("§6§m----------§b [$page/${entries.size}] §6§m--------")
            )
        }
    }
}