package de.bergwerklabs.friends.client.bungee.common

import de.bergwerklabs.atlantis.client.base.playerdata.PlayerdataSet
import de.bergwerklabs.atlantis.client.base.playerdata.SettingsFlag
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Yannic Rieger on 17.12.2017.
 * <p>
 * @author Yannic Rieger
 */
class PlayerSettingsWrapper {
    
    private val playerdataCache = ConcurrentHashMap<UUID, PlayerdataSet>()
    
    fun canReceiveInvites(player: UUID): Boolean {
        val set = this.loadSettings(player)
        return set.playerSettings!!.isSet(SettingsFlag.GLOBAL_FRIEND_REQUESTS_ENABLED)
    }
    
    fun isOnlineStatusEnabled(player: UUID): Boolean {
        val set = this.loadSettings(player)
        return set.playerSettings!!.isSet(SettingsFlag.GLOBAL_CHAT_FRIEND_NOTIFICATIONS_ENABLED)
    }
    
    private fun loadSettings(player: UUID): PlayerdataSet {
        val set: PlayerdataSet = PlayerdataSet(player)
        set.loadAndWait()
        return set
    }
}