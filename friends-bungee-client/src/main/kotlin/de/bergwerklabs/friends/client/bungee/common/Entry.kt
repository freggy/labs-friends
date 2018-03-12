package de.bergwerklabs.friends.client.bungee.common

import de.bergwerklabs.api.cache.pojo.players.online.OnlinePlayerCacheEntry
import de.bergwerklabs.atlantis.client.base.resolve.PlayerResolver
import net.md_5.bungee.api.ChatColor
import java.util.*

/**
 * Created by Yannic Rieger on 03.12.2017.
 * <p>
 * @author Yannic Rieger
 */
data class Entry(val name: String, val rankColor: ChatColor, val uuid: UUID)