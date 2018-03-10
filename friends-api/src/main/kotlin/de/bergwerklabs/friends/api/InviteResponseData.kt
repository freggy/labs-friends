package de.bergwerklabs.friends.api

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse

/**
 * Created by Yannic Rieger on 10.03.2018.
 * <p>
 * @author Yannic Rieger
 */
data class InviteResponseData(
    val sender: PlayerNameToUuidMapping,
    val receiver: PlayerNameToUuidMapping,
    val response: FriendRequestResponse
)