package de.bergwerklabs.friends.client.bungee

import de.bergwerklabs.api.cache.pojo.PlayerNameToUuidMapping
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
class FriendRequestListener {
    
    fun onInvite(sender: PlayerNameToUuidMapping, invited: PlayerNameToUuidMapping) {
        
        friendsClient!!.proxy.getPlayer(invited.uuid)?.let { inv ->
            val initialSenderName = sender.name
            
            // nasty little workaround to get the fancy message centered as well.
            val spaces = MessageUtil.getSpacesToCenter("§a[ANNEHMEN]§6 | §c[ABLEHNEN]")
            val builder = StringBuilder()
            for (i in 0..spaces) builder.append(" ")
    
            val message = ComponentBuilder("$builder§a[ANNEHMEN]")
                .color(ChatColor.GREEN)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend accept $initialSenderName"))
                .append(" ❘ ").color(ChatColor.GOLD)
                .append("[ABLEHNEN]").color(ChatColor.RED).event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/friend deny $initialSenderName"))
                .create()
    
            MessageUtil.sendCenteredMessage(inv, "§6§m-------§b Freunschaftsanfrage §6§m-------")
            MessageUtil.sendCenteredMessage(inv, " ")
            MessageUtil.sendCenteredMessage(inv, "§7Du hast eine Anfrage von §a$initialSenderName §7erhalten.")
            MessageUtil.sendCenteredMessage(inv," ")
            inv.sendMessage(ChatMessageType.CHAT, *message)
            MessageUtil.sendCenteredMessage(inv," ")
            MessageUtil.sendCenteredMessage(inv, "§6§m--------------")
        }
    }
}