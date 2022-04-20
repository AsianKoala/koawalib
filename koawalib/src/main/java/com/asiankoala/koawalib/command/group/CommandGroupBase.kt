package com.asiankoala.koawalib.command.group

import com.asiankoala.koawalib.command.commands.Command
import com.asiankoala.koawalib.command.commands.CommandBase
import java.util.*

/**
 * CommandGroups are used to run multiple commands. To create a custom CommandGroup, extend this class
 */
abstract class CommandGroupBase : CommandBase(), Command {
    companion object {
        private val groupCommands: MutableSet<Command> = Collections.newSetFromMap(WeakHashMap())

        /**
         * Add commands internally to grouped commands set
         * @param commands Commands to register with all grouped
         */
        fun registerGroupedCommands(vararg commands: Command) {
            groupCommands.addAll(commands)
        }

        /**
         * Throw exception if argument commands are already grouped
         * @param commands Commands to assert non-grouped
         */
        fun assertUngrouped(vararg commands: Command) {
            if (!Collections.disjoint(groupCommands, commands.toList())) {
                throw IllegalArgumentException("Commands cannot be added to more than one CommandGroup")
            }
        }

        /**
         * @return All grouped commands
         */
        fun getGroupedCommands(): Set<Command> {
            return groupCommands
        }
    }

    /**
     * Add commands to a group
     * @param commands Commands to add to the group
     */
    abstract fun addCommands(vararg commands: Command)
}
