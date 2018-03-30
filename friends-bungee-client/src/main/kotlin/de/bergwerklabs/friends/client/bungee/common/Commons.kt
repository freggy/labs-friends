package de.bergwerklabs.friends.client.bungee.common

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.api.cache.pojo.players.online.PlayerEntry
import de.bergwerklabs.atlantis.api.friends.Friend
import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.api.friends.FriendLogoutPacket
import de.bergwerklabs.friends.api.FriendsApi
import me.lucko.luckperms.LuckPerms
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*

internal fun sendMessageToFriends(player: ProxiedPlayer, isLogin: Boolean) {
    FriendsApi.getFriendList(player.uniqueId).thenAccept { friends ->
        friends.forEach { friend ->
            val mapping = PlayerNameToUuidMapping(player.name, player.uniqueId)
            if (isLogin) {
                packageService.sendPackage(FriendLoginPacket(mapping, friend.mapping))
            }
            else packageService.sendPackage(FriendLogoutPacket(mapping, friend.mapping))
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
internal fun list(pages: List<Entry>, player: ProxiedPlayer, isFriendList: Boolean) {
    pages.stream()
        .filter(Objects::nonNull)
        .forEach { obj -> displayInfo(player, obj, isFriendList) }
}


internal fun compareFriends(entry1: Pair<ChatColor, PlayerNameToUuidMapping>, entry2: Pair<ChatColor, PlayerNameToUuidMapping>): Int {
    val result = Integer.compare(entry1.first.ordinal, entry2.first.ordinal)
    // compare UUIDs since we always get a different order each time we request the list
    if (result == 0) return entry1.second.uuid.compareTo(entry2.second.uuid)
    return result
}


private fun displayInfo(player: ProxiedPlayer, entry: Entry, isFriendList: Boolean) {
    val message = if (isFriendList) friendListComps(entry.name, entry.rankColor, entry.playerEntry)
    else pendingComps(
        entry.name, entry.rankColor
    )
    player.sendMessage(ChatMessageType.CHAT, *message.create())
}

private fun friendListComps(friendName: String, friendRankColor: ChatColor, online: Optional<PlayerEntry>): ComponentBuilder {
    val comps = ComponentBuilder("✖").color(ChatColor.RED).event(
        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend remove $friendName")
    )
        .event(
            HoverEvent(
                HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Entferne $friendName aus der Freundesliste.")
            )
        )
        
        .append("✸").color(ChatColor.GOLD).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party invite $friendName"))
        .event(
            HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Lade $friendName in eine Party ein"))
        )
        
        .append("➥").color(ChatColor.AQUA).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend tp $friendName"))
        .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Joine $friendName nach")))
        
        .append(" $friendName").color(friendRankColor)
        .append(" - ").color(ChatColor.DARK_GRAY)
    
    
    
    if (!online.isPresent) {
        comps.append("OFFLINE")
            .color(ChatColor.RED)
            .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Im Limbus")))
    }
    else {
        val current = online.get()
        comps.append("ONLINE")
            .color(ChatColor.GREEN)
            .event(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText(
                        "§7${current.server.service}\n§7${generator.generate(current.server.id)}"
                    )
                )
            )
    }
    return comps
}

private fun pendingComps(friendName: String, friendRankColor: ChatColor): ComponentBuilder {
    return ComponentBuilder("✖").color(ChatColor.RED)
        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny $friendName"))
        .event(
            HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Lehne Anfrage von $friendName ab."))
        )
        .append("✚").color(ChatColor.GREEN)
        .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept $friendName"))
        .event(
            HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Nehme Anfrage von $friendName an."))
        )
        .append(" $friendName").color(friendRankColor)
}

internal fun getRankColor(uuid: UUID) = ChatColor.getByChar(bridge.getGroupPrefix(uuid)[1])

internal fun getLoginMessage(name: String, color: ChatColor): Array<BaseComponent> {
    val login = TextComponent.toLegacyText(*getLoginOrOutMessage(name, color, "online", ChatColor.GREEN).create())
    return TextComponent.fromLegacyText("$prefix$login")
}

internal fun getLogoutMessage(name: String, color: ChatColor): Array<BaseComponent> {
    val out = TextComponent.toLegacyText(*getLoginOrOutMessage(name, color, "offline", ChatColor.RED).create())
    return TextComponent.fromLegacyText("$prefix$out")
}

internal fun getColorBlocking(uuid: UUID): String {
    val perms = LuckPerms.getApi()
    
    val user = if (perms.userManager.isLoaded(uuid)) {
        perms.userManager.getUser(uuid)!!
    }
    else perms.userManager.loadUser(uuid).join()
    
    val group = if (perms.isGroupLoaded(user.primaryGroup)) {
        perms.groupManager.getGroup(user.primaryGroup)!!
    }
    else perms.groupManager.loadGroup(user.primaryGroup).join().orElse(perms.groupManager.getGroup("default"))
    
    return group.permissions.find { node -> node.isPrefix }!!.prefix.value
}

private fun getLoginOrOutMessage(
    name: String,
    color: ChatColor,
    loginOrOut: String,
    logColor: ChatColor
): ComponentBuilder {
    return ComponentBuilder(name).color(color)
        .append(" ist ").color(ChatColor.GRAY)
        .append(loginOrOut).color(logColor)
    
}
