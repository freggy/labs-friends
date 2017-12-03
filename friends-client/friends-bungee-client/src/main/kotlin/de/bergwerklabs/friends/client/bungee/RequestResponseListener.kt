package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.friends.api.FriendRequestResponseListener
import java.util.*

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class RequestResponseListener : FriendRequestResponseListener {
    
    override fun onResponse(response: FriendRequestResponse, sender: UUID, receiver: UUID) {
        friendsClient!!.proxy.getPlayer(sender).let { player ->
            PlayerResolver.resolveUuidToName(receiver).ifPresent {
                val messenger = friendsClient!!.messenger
                val color = friendsClient!!.zBridge.getRankColor(receiver)
                when (response) {
                    FriendRequestResponse.ACCEPTED          -> messenger.message("§a✚ $color$it§7 hat deine Freunschaftsanfrage angenommen.", player)
                    FriendRequestResponse.DENIED            -> messenger.message("§c✖ $color$it§7 hat deine Freunschaftsanfrage abgelehnt.", player)
                    FriendRequestResponse.FRIEND_LIST_FULL  -> messenger.message("§cDeine Freundesliste ist voll.", player)
                    FriendRequestResponse.ALREADY_REQUESTED -> messenger.message("§cDu hast diesem Spieler bereits eine Anfrage geschickt.", player)
                    FriendRequestResponse.ALREADY_FRIENDS   -> messenger.message("§cDieser Spieler ist in bereits in deiner Freundesliste.", player)
                    else                                    -> messenger.message("§4Da ist wohl etwas drastisch schiefgelaufen... :(", player)
                }
            }
        }
    }
}