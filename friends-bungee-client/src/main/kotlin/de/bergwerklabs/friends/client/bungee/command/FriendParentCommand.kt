package de.bergwerklabs.friends.client.bungee.command

import de.bergwerklabs.framework.commons.bungee.command.BungeeCommand
import de.bergwerklabs.framework.commons.bungee.command.BungeeParentCommand

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendParentCommand(name: String?, description: String?, usage: String?, defaultCommand: BungeeCommand?, vararg childCommands: BungeeCommand?) : BungeeParentCommand(name, description, usage, defaultCommand, *childCommands)