package de.bergwerklabs.friends.server

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.friends.FriendInviteRequestPacket
import de.bergwerklabs.atlantis.api.friends.FriendInviteResponsePacket
import de.bergwerklabs.atlantis.api.friends.PlayerLoginPacket
import de.bergwerklabs.atlantis.api.friends.PlayerLogoutPacket
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import java.util.*

internal val service = AtlantisPackageService(
        PlayerLogoutPacket::class.java,
        PlayerLoginPacket::class.java,
        FriendInviteResponsePacket::class.java,
        FriendInviteRequestPacket::class.java
)

// Contains the friends of a specific player
internal val uuidToFriends = HashMap<UUID, MutableSet<FriendEntry>>()

// Contains the pending friend requests of a player.
internal val uuidToPending = HashMap<UUID, MutableSet<RequestEntry>>()

// Contains the friend requests sent by that player
internal val uuidToRequested = HashMap<UUID, MutableSet<RequestEntry>>()