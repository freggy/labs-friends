package de.bergwerklabs.friends.client.bungee.command

import com.google.common.collect.Iterables
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.common.Entry
import de.bergwerklabs.friends.client.bungee.common.list
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.sql.Timestamp

/**
 * Created by Yannic Rieger on 03.12.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendListInvitesCommand : BungeeCommand {
    
    private val pageSize = 5
    
    override fun getUsage() = "/friend invites"
    
    override fun getName() = "invites"
    
    override fun getDescription() = "TODO" // TODO: descrpt.
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
    
        if (sender is ProxiedPlayer) {
            val pending = FriendsApi.getPendingRequests(sender.uniqueId)
        
            if (pending.isNotEmpty()) {
                var page = 1
                if (args!!.isNotEmpty()) {
                    if (args!![0].isNullOrEmpty() || args[0].isBlank()) {
                        friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                        return
                    }
                    page = args[0].toInt()
                }
            
                val sorted = pending.sortedWith(kotlin.Comparator { entry1, entry2 -> Timestamp.valueOf(entry1.created).compareTo(Timestamp.valueOf(entry2.created)) })
                val pages = Iterables.partition(sorted, pageSize).toList()
            
                if (page > pages.size || page <= 0) {
                    friendsClient!!.messenger.message("§cSeitenzahl zu groß oder zu klein.", sender)
                    return
                }
            
                sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------§b Anfragen §6§m-------"))
            
                try {
                    val converted = pages
                            .map { pendingPage -> pendingPage
                            .map { pending -> Entry(
                                    PlayerResolver.getOnlinePlayerCacheEntry(pending.requester.toString()),
                                    PlayerResolver.resolveUuidToName(pending.requester).orElse(":("),
                                    friendsClient!!.zBridge.getRankColor(pending.requester))
                            }
                    }
                    list(page, converted, sender, true)
                }
                catch (ex: Exception) {
                    ex.printStackTrace()
                }
            
                sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m----------§b [$page/${(Math.ceil(pending.size.toDouble() / pageSize)).toInt()}] §6§m-----------"))
            }
            else {
                friendsClient!!.messenger.message("§cDu hast keine ausstehenden Anfragen.", sender)
                return
            }
        }
    }
}