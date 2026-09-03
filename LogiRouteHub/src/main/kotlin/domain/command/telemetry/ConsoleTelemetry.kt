package domain.command.telemetry

class ConsoleTelemetry : CommandTelemetry {

    override fun recordCommandExecution(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        println("Command executed: $commandType | " +
                    "undoStack=$undoStackSize | " +
                    "redoStack=$redoStackSize")
    }

    override fun recordUndo(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        println("Undo: $commandType | " +
                    "undoStack=$undoStackSize | " +
                    "redoStack=$redoStackSize")
    }

    override fun recordRedo(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    ) {
        println("Redo: $commandType | " +
                    "undoStack=$undoStackSize | " +
                    "redoStack=$redoStackSize")
    }

    override fun recordStateTransition(
        commandType: String,
        beforeState: String,
        afterState: String
    ) {
        println("State transition: $commandType\n" +
                    "Before: $beforeState\n" +
                    "After: $afterState"
        )
    }
}
