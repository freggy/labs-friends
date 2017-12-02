package de.bergwerklabs.friends.client.bungee.command

import com.google.common.collect.Iterables
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.players.online.OnlinePlayerCacheEntry
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.friends.api.FriendsApi
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

/**
 * Created by Yannic Rieger on 04.11.2017.
 *
 * @author Yannic Rieger
 */
class FriendListCommand : BungeeCommand {
    
    private val pageSize = 10
    
    override fun getName() = "list"
    
    override fun getDescription() = "Listet alle deine Freunde auf."
    
    override fun getUsage() = "/friend list"
    
    override fun execute(sender: CommandSender?, args: Array<out String>?) {
        if (sender is ProxiedPlayer) {
            val friendList = FriendsApi.retrieveFriendInfo(sender.uniqueId).friendList
            println(friendList.size)
            if (friendList.isNotEmpty()) {
                sender.sendMessage("§6§m-------§b Freundesliste §6§m-------")
                
                var page = 1
                if (args!!.isNotEmpty()) {
    
                    if (args!![0].isNullOrEmpty() || args[0].isBlank()) {
                        friendsClient!!.messenger.message("§cEin Fehler ist aufgetreten.", sender)
                        return
                    }
                    page = args[0].toInt()
                }
            
                try {
                    this.listFriends(page, friendList, sender)
                }
                catch (ex: IllegalArgumentException) {
                    ex.printStackTrace()
                }
            
                sender.sendMessage(ChatMessageType.CHAT, *TextComponent.fromLegacyText("§6§m----------§b [$page/${(Math.ceil(friendList.size.toDouble() / pageSize)).toInt()}] §6§m----------"))
            }
        }
    }
    
    /**
     * Lists 10 friends at a time and displays them to the player.
     * The number of player displayed is defined by [pageSize] which is currently 10.
     *
     * @param page    page the player wants to navigate to.
     * @param friends set containing all the players friends.
     * @param player  player that executed the command.
     */
    private fun listFriends(page: Int, friends: Set<FriendEntry>, player: ProxiedPlayer) {
        val pages = Iterables.toArray(Iterables.paddedPartition(friends, pageSize), List::class.java) as Array<List<FriendEntry?>>
        
        if (page > pages.size || page <= 0) {
            friendsClient!!.messenger.message("§cSeitenzahl zu groß oder zu klein.", player)
            return
        }
        
        pages[page - 1]
                .stream()
                .filter(Objects::nonNull)
                .map { entry ->
                    object {
                        val onlineInfo = PlayerResolver.getOnlinePlayerCacheEntry(entry!!.friend.toString())
                        val friendName =  PlayerResolver.resolveUuidToName(entry!!.friend)
                        val friendUuid = entry!!.friend
                        val friendRankColor = friendsClient!!.zBridge.getRankColor(friendUuid)
                    }
                }
                .sorted  { obj1, obj2 -> Integer.compare(obj1.friendRankColor.ordinal, obj2.friendRankColor.ordinal) }
                .forEach { obj -> this.displayInfo(player, obj.onlineInfo, obj.friendName, obj.friendUuid, obj.friendRankColor) }
    }
    
    /**
     *
     */
    private fun displayInfo(player: ProxiedPlayer, onlineInfo: Optional<OnlinePlayerCacheEntry>, friendName: Optional<String>, friendUuid: UUID, friendRankColor: ChatColor) {
        friendName.ifPresent {
            val message = ComponentBuilder("✖").color(ChatColor.RED).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend remove $it"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Entferne $it aus der Freundesliste.")))
                
                    .append("✸").color(ChatColor.GOLD).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party invite $it"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,  TextComponent.fromLegacyText("Lade $it in eine Party ein")))
                
                    .append("➥").color(ChatColor.AQUA).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend jump $it"))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Joine $it nach")))
                
                    .append(" $it").color(friendRankColor)
                    .append(" - ").color(ChatColor.DARK_GRAY)
        
            if (onlineInfo.isPresent) {
                val currentServer = onlineInfo.get().currentServer
                
                // TODO: generate display name
                message.append(currentServer.service).color(ChatColor.GRAY)
                       .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Id: ${currentServer.containerId}")))
            }
            else message.append("OFFLINE")
                        .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Im Limbus")))
                        .color(ChatColor.RED)
            player.sendMessage(ChatMessageType.CHAT, *message.create())
        }
    }
}