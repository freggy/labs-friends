package de.bergwerklabs.friends.client.bungee.command

import com.google.common.collect.Iterables
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.common.Entry
import de.bergwerklabs.friends.client.bungee.common.compareFriends
import de.bergwerklabs.friends.client.bungee.common.list
import de.bergwerklabs.friends.client.bungee.friendsClient
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
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            if (friendList.isNotEmpty()) {
                var page = 1
    
                if (args!!.isNotEmpty()) {
                    if (args!![0].isNullOrEmpty() || args[0].isBlank()) {
                        friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                        return
                    }
                    page = args[0].toInt()
                }
                // PlayerResolver methods are blocking the main thread
                friendsClient!!.runAsync {
                    val sorted = friendList
                            .map { friend -> Entry(
                                    PlayerResolver.resolveUuidToName(friend.friend).orElse(":("),
                                    friendsClient!!.zBridge.getRankColor(friend.friend))
                            }
                            .sortedWith(kotlin.Comparator { obj1, obj2 -> compareFriends(obj1, obj2)  })
        
                    val pages = Iterables.partition(sorted, pageSize).toList()
                    
                    if (page > pages.size || page <= 0) {
                        friendsClient!!.messenger.message("§cSeitenzahl zu groß oder zu klein.", sender)
                        return@runAsync
                    }
                    
                        sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m-------§b Freundesliste §6§m-------"))
                        list(page, pages, sender, true)
                        sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m----------§b [$page/${(Math.ceil(friendList.size.toDouble() / pageSize)).toInt()}] §6§m-----------"))
                    }
            }
            else {
                friendsClient!!.messenger.message("§c${funnySentences[Random().nextInt(funnySentences.size)]}", sender)
                return
            }
        }
    }
    
    
    /*
    
    
    private fun listFriends(page: Int, pages: List<List<FriendEntry>>, player: ProxiedPlayer) {
        pages[page - 1]
                .stream()
                .filter(Objects::nonNull)
                .map { entry ->
                    object {
                        val onlineInfo = PlayerResolver.getOnlinePlayerCacheEntry(entry!!.friend.toString())
                        val friendName =  PlayerResolver.resolveUuidToName(entry!!.friend).orElse(":(")
                        val friendUuid = entry!!.friend
                        val friendRankColor = friendsClient!!.zBridge.getRankColor(friendUuid)
                    }
                }
                .sorted  { obj1, obj2 -> Integer.compare(obj1.friendRankColor.ordinal, obj2.friendRankColor.ordinal) }
                .forEach { obj -> this.displayInfo(player, obj.onlineInfo, obj.friendName, obj.friendRankColor) }
    }
    
    /**
     *
     */
    private fun displayInfo(player: ProxiedPlayer, onlineInfo: Optional<OnlinePlayerCacheEntry>, friendName: String, friendRankColor: ChatColor) {
        
        val message = ComponentBuilder("✖").color(ChatColor.RED).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend remove $friendName"))
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Entferne $friendName aus der Freundesliste.")))
                
                .append("✸").color(ChatColor.GOLD).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party invite $friendName"))
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,  TextComponent.fromLegacyText("Lade $friendName in eine Party ein")))
                
                .append("➥").color(ChatColor.AQUA).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend jump $friendName"))
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Joine $friendName nach")))
                
                .append(" $friendName").color(friendRankColor)
                .append(" - ").color(ChatColor.DARK_GRAY)
        
        if (onlineInfo.isPresent) {
            message.append("ONLINE").color(ChatColor.GRAY)
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("LOLOLOLOLOLOL")))
            onlineInfo.get().currentServer?.let {
                // TODO: generate display name
                message.append(it.service).color(ChatColor.GRAY)
                        .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Id: ${it.containerId}")))
            }
        }
        else message.append("OFFLINE")
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Im Limbus")))
                    .color(ChatColor.RED)
        player.sendMessage(ChatMessageType.CHAT, *message.create())
    } */
}