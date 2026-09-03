package domain.command.telemetry

interface CommandTelemetry {

    fun recordCommandExecution(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    )

    fun recordUndo(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    )

    fun recordRedo(
        commandType: String,
        undoStackSize: Int,
        redoStackSize: Int
    )

    fun recordStateTransition(
        commandType: String,
        beforeState: String,
        afterState: String
    )
}
