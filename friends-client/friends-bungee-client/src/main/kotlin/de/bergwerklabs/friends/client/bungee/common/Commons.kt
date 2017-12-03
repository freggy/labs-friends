package de.bergwerklabs.friends.client.bungee.common

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.players.online.OnlinePlayerCacheEntry
import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.api.friends.FriendLogoutPacket
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.friends.client.bungee.friendsClient
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

internal fun sendMessageToFriends(friendList: Set<FriendEntry>,
                                  service:    AtlantisPackageService,
                                  proxy:      ProxyServer,
                                  player:     ProxiedPlayer,
                                  onLogin:    Boolean) {
    friendList.forEach { entry ->
        val playerOnServer = proxy.getPlayer(entry.friend)
        
        if (playerOnServer != null) {
            val message = if (onLogin) {
                getLoginMessage(player.name, friendsClient!!.zBridge.getRankColor(player.uniqueId))
            }
            else getLogoutMessage(player.name, friendsClient!!.zBridge.getRankColor(player.uniqueId))
            
            friendsClient!!.messenger.message(TextComponent.toLegacyText(*message), playerOnServer)
        }
        else {
            
            val packet = if (onLogin) {
                FriendLoginPacket(PlayerNameToUuidMapping(player.name, player.uniqueId), entry.friend)
            }
            else FriendLogoutPacket(PlayerNameToUuidMapping(player.name, player.uniqueId), entry.friend)
            
            service.sendPackage(packet)
        }
    }
}

/**
 * Lists 10 friends at a time and displays them to the player.
 * The number of player displayed is defined by a [pageSize].
 *
 * @param page    page the player wants to navigate to.
 * @param pages   contains all the players friends.
 * @param player  player that executed the command.
 */
internal fun list(page: Int, pages: List<List<Entry>>, player: ProxiedPlayer, isFriendList: Boolean) {
    pages[page - 1]
            .stream()
            .filter(Objects::nonNull)
            .sorted  { obj1, obj2 -> Integer.compare(obj1.rankColor.ordinal, obj2.rankColor.ordinal) }
            .forEach { obj -> displayInfo(player, obj.onlineInfo, obj.name, obj.rankColor, isFriendList) }
}

private fun displayInfo(player:          ProxiedPlayer,
                        onlineInfo:      Optional<OnlinePlayerCacheEntry>,
                        friendName:      String,
                        friendRankColor: ChatColor,
                        isFriendList:    Boolean) {
    
    val message = if (isFriendList) friendListComps(friendName, friendRankColor) else pendingComps(friendName, friendRankColor)
    
    if (onlineInfo.isPresent) {
        message.append("ONLINE").color(ChatColor.GRAY)
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("LOLOLOLOLOLOL")))
        // TODO: use when proper information is in online player cache
        /*
        onlineInfo.get().currentServer?.let {
            // TODO: generate display name
            message.append(it.service).color(ChatColor.GRAY)
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Id: ${it.containerId}")))
        } */
    }
    else message.append("OFFLINE")
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Im Limbus")))
            .color(ChatColor.RED)

    player.sendMessage(ChatMessageType.CHAT, *message.create())
}

private fun friendListComps(friendName: String, friendRankColor: ChatColor): ComponentBuilder {
    return ComponentBuilder("✖").color(ChatColor.RED).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend remove $friendName"))
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Entferne $friendName aus der Freundesliste.")))
            
            .append("✸").color(ChatColor.GOLD).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party invite $friendName"))
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT,  TextComponent.fromLegacyText("Lade $friendName in eine Party ein")))
            
            .append("➥").color(ChatColor.AQUA).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend jump $friendName"))
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Joine $friendName nach")))
            
            .append(" $friendName").color(friendRankColor)
            .append(" - ").color(ChatColor.DARK_GRAY)
}

private fun pendingComps(friendName: String, friendRankColor: ChatColor): ComponentBuilder {
    return ComponentBuilder("✖").color(ChatColor.RED)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny $friendName"))
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Lehne Anfrage von $friendName ab.")))
            .append("✚").color(ChatColor.GREEN)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept $friendName"))
                .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Nehme Anfrage von $friendName an.")))
            .append(" $friendName").color(friendRankColor)
            .append(" - ").color(ChatColor.DARK_GRAY)
}

internal fun getLoginMessage(name: String, color: ChatColor): Array<BaseComponent> {
    return getLoginOrOutMessage(name, color, "online", ChatColor.GREEN)
            .append(" [").color(ChatColor.GRAY)
            .append("EINLADEN").color(ChatColor.GOLD)
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party invite $name"))
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("$name zu einer party einladen.")))
            .append("]").color(ChatColor.GRAY).create()
}

internal fun getLogoutMessage(name: String, color: ChatColor): Array<BaseComponent> {
    return getLoginOrOutMessage(name, color, "offline", ChatColor.RED).create()
}

private fun getLoginOrOutMessage(name: String, color: ChatColor, loginOrOut: String, logColor: ChatColor): ComponentBuilder {
    return ComponentBuilder(name).color(color)
            .append(" ist ").color(ChatColor.GRAY)
            .append(loginOrOut).color(logColor)
    
}
