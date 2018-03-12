package de.bergwerklabs.friends.client.bungee.common

import de.bergwerklabs.atlantis.api.friends.FriendLoginPacket
import de.bergwerklabs.atlantis.api.friends.FriendLogoutPacket
import de.bergwerklabs.atlantis.api.friends.FriendServerInviteRequestPacket
import de.bergwerklabs.atlantis.api.friends.invite.FriendInviteServerResponse
import de.bergwerklabs.atlantis.client.base.util.AtlantisPackageService
import de.bergwerklabs.framework.permissionbridge.PermissionBridge

internal val prefix = "§6>> §eFriends §6❘§7 "

internal val packageService = AtlantisPackageService(
    FriendServerInviteRequestPacket::class.java,
    FriendInviteServerResponse::class.java,
    FriendLoginPacket::class.java,
    FriendLogoutPacket::class.java
)

internal lateinit var bridge: PermissionBridge