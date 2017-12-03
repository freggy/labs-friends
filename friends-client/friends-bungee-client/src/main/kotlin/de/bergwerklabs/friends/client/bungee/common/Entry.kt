package de.bergwerklabs.friends.client.bungee.common

import de.bergwerklabs.api.cache.pojo.players.online.OnlinePlayerCacheEntry
import net.md_5.bungee.api.ChatColor
import java.util.*

/**
 * Created by Yannic Rieger on 03.12.2017.
 * <p>
 * @author Yannic Rieger
 */
data class Entry(public val onlineInfo: Optional<OnlinePlayerCacheEntry>, val name: String, val rankColor: ChatColor)