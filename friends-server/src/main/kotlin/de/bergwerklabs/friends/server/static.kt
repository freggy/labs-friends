package de.bergwerklabs.friends.server

import de.bergwerklabs.api.cache.pojo.friends.FriendEntry
import de.bergwerklabs.api.cache.pojo.friends.RequestEntry
import de.bergwerklabs.atlantis.api.friends.FriendRemovePacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteClientResponsePacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerRequest
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.atlantis.api.friends.server.FriendlistRequestPacket
import de.bergwerklabs.atlantis.api.friends.server.PendingInvitesRequestPacket
import de.bergwerklabs.atlantis.api.friends.server.PlayerLoginPacket
import de.bergwerklabs.atlantis.api.friends.server.PlayerLogoutPacket
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import java.util.*

// TODO: add pending

internal val service = AtlantisPackageService(
        PlayerLogoutPacket::class.java,
        PlayerLoginPacket::class.java,
        FriendlistRequestPacket::class.java,
        FriendInviteClientRequestPacket::class.java,
        FriendInviteClientResponsePacket::class.java,
        FriendRemovePacket::class.java,
        PendingInvitesRequestPacket::class.java
)

// Contains the friends of a specific player
internal val uuidToFriends = HashMap<UUID, MutableSet<FriendEntry>>()

// Contains the pending friend requests of a player.
internal val uuidToPending = HashMap<UUID, MutableSet<RequestEntry>>()

// Contains the friend requests sent by that player
internal val uuidToRequested = HashMap<UUID, MutableSet<RequestEntry>>()