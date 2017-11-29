package de.bergwerklabs.friends.client.bukkit

import de.bergwerklabs.atlantis.api.friends.FriendInviteRequestPacket
import de.bergwerklabs.atlantis.client.base.PlayerResolver
import de.bergwerklabs.atlas.api.AtlasPacketListener
import de.bergwerklabs.framework.commons.bungee.chat.text.MessageUtil
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder

/**
 * Created by Yannic Rieger on 05.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendRequestListener : AtlasPacketListener<FriendInviteRequestPacket> {
    
    override fun onPacketReceived(packet: FriendInviteRequestPacket) {
        friendsClient!!.proxy.getPlayer(packet.receiver).let {
            val initialSenderName = PlayerResolver.resolveUuidToName(packet.sender).get()
        
            // nasty little workaround to get the fancy message centered as well.
            val spaces = MessageUtil.getSpacesToCenter("§a[ANNEHMEN]§6 | §c[ABLEHNEN]")
            val builder = StringBuilder()
            for (i in 0..spaces) builder.append(" ")
        
            val message = ComponentBuilder("$builder§a[ANNEHMEN]").color(ChatColor.GREEN).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept"))
                    .append(" ❘ ").color(ChatColor.GOLD)
                    .append("[ABLEHNEN]").color(ChatColor.RED).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny"))
                    .create()
        
            MessageUtil.sendCenteredMessage(it, "§6§m-------§b Freunschaftsanfrage§6§m-------")
            MessageUtil.sendCenteredMessage(it, " ")
            MessageUtil.sendCenteredMessage(it, "§7Du hast eine Anfrage von §a$initialSenderName §7erhalten.")
            MessageUtil.sendCenteredMessage(it," ")
            it.sendMessage(ChatMessageType.CHAT, *message)
            MessageUtil.sendCenteredMessage(it," ")
            MessageUtil.sendCenteredMessage(it, "§6§m--------------")
        }
    }
}