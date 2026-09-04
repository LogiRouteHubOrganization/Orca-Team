package domain.command.telemetry

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
        beforeState: String,
        afterState: String
    ) {
        println()
        println("  STATE CHANGE | $commandType")
        println("  Before:")
        printIndentedState(beforeState)

        println("  After:")
        printIndentedState(afterState)
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
