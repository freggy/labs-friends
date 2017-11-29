package de.bergwerklabs.friends.client.bukkit

import de.bergwerklabs.atlantis.api.friends.FriendInviteResponsePacket
import de.bergwerklabs.atlantis.api.friends.FriendRequestResponse
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlas.api.AtlasPacketListener
import org.bukkit.Bukkit

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class RequestResponseListener : AtlasPacketListener<FriendInviteResponsePacket> {
    
    override fun onPacketReceived(packet: FriendInviteResponsePacket) {
        friendsClient.proxy.getPlayer(packet.receiver).let { player ->
            PlayerResolver.resolveUuidToName(packet.sender).ifPresent {
                val messenger = friendsClient!!.messenger
                val color = friendsClient!!.getRankColor(packet.sender)
                when (packet.requestResponse) {
                    FriendRequestResponse.ACCEPTED         -> messenger.message("§a✚ $color$it§7 hat deine Freunschaftsanfrage angenommen.", player)
                    FriendRequestResponse.DENIED           -> messenger.message("§c✖ $color$it§7 hat deine Freunschaftsanfrage abgelehnt.", player)
                    FriendRequestResponse.FRIEND_LIST_FULL -> messenger.message("§cDeine Freundesliste ist voll.", player)
                    else                                   -> messenger.message("§4Da ist wohl etwas drastisch schiefgelaufen... :(", player)
                }
            }
        }
    }
}