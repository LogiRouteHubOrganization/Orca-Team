package domain.command.telemetry

import domain.command.CommandState

class ConsoleTelemetry : CommandTelemetry {

    override fun recordCommandExecution(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        printHistoryEvent(
            action = "EXECUTE",
            commandType = commandType,
            undoStackSize = undoStackSize,
            redoStackSize = redoStackSize
        )
    }

    override fun recordUndo(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        printHistoryEvent(
            action = "UNDO",
            commandType = commandType,
            undoStackSize = undoStackSize,
            redoStackSize = redoStackSize
        )
    }

    override fun recordRedo(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        printHistoryEvent(
            action = "REDO",
            commandType = commandType,
            undoStackSize = undoStackSize,
            redoStackSize = redoStackSize
        )
    }

    override fun recordStateTransition(
        commandType: String,
        beforeState: CommandState,
        afterState: CommandState
    ) {
        println()
        println("  STATE CHANGE | $commandType")
        println("  Before:")
        printIndentedState(beforeState.describe())

        println("  After:")
        printIndentedState(afterState.describe())
    }

    private fun printHistoryEvent(
        action: String,
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        println()
        println("  [$action] $commandType")
        println(
            "  History -> " +
                    "Undo Stack: $undoStackSize | " +
                    "Redo Stack: $redoStackSize"
        )
    }

    private fun printIndentedState(
        state: String
    ) {
        state.lines().forEach { line ->
            println("    $line")
        }
    }
}

