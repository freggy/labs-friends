package de.bergwerklabs.friends.client.bukkit.command

import de.bergwerklabs.framework.commons.spigot.command.ChildCommand
import de.bergwerklabs.framework.commons.spigot.command.ParentCommand
import org.bukkit.command.CommandExecutor

/**
 * Created by Yannic Rieger on 04.11.2017.
 * <p>
 * @author Yannic Rieger
 */
class FriendParentCommand(command: String?, executor: CommandExecutor?, vararg childCommands: ChildCommand?) : ParentCommand(command, executor, *childCommands)