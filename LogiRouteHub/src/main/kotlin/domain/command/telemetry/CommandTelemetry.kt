package domain.command.telemetry

import domain.command.CommandState

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
        beforeState: CommandState,
        afterState: CommandState
    )
}
