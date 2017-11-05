package de.bergwerklabs.friends.client.bukkit

import de.bergwerklabs.atlantis.api.friends.FriendInviteRequestPacket
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlas.api.AtlasPacketListener
import de.bergwerklabs.commons.spigot.chat.MessageUtil
import mkremins.fanciful.FancyMessage
import org.bukkit.Bukkit
import org.bukkit.ChatColor

/**
 * Created by Yannic Rieger on 05.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendRequestListener : AtlasPacketListener<FriendInviteRequestPacket> {
    
    override fun onPacketReceived(packet: FriendInviteRequestPacket) {
        Bukkit.getPlayer(packet.receiver).let {
            val initialSenderName = PlayerResolver.resolveUuidToName(packet.sender).get()
        
            // nasty little workaround to get the fancy message centered as well.
            val spaces = MessageUtil.getSpacesToCenter("§a[ANNEHMEN]§6 | §c[ABLEHNEN]")
            val builder = StringBuilder()
            for (i in 0..spaces) builder.append(" ")
        
            val message = FancyMessage("$builder§a[ANNEHMEN]").color(ChatColor.GREEN).command("/friend accept")
                    .then(" ❘ ").color(ChatColor.GOLD)
                    .then("[ABLEHNEN]").color(ChatColor.RED).command("/friend deny")
        
            MessageUtil.sendCenteredMessage(it, "§6§m-------§b Freunschaftsanfrage§6§m-------")
            MessageUtil.sendCenteredMessage(it, " ")
            MessageUtil.sendCenteredMessage(it, "§7Du hast eine Anfrage von §a$initialSenderName §7erhalten.")
            MessageUtil.sendCenteredMessage(it," ")
            message.send(it)
            MessageUtil.sendCenteredMessage(it," ")
            MessageUtil.sendCenteredMessage(it, "§6§m--------------")
        }
    }
}