package domain.command

import domain.command.telemetry.CommandTelemetry

class CommandInvoker(
    private val telemetry: CommandTelemetry
) {
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()

    suspend fun execute(command: Command) {
        val isExecuted = command.execute()

        if (isExecuted) {
            undoStack.add(command)
            redoStack.clear()

            telemetry.recordCommandExecution(
                commandType = command.commandType,
                undoStackSize = undoStack.size,
                redoStackSize = redoStack.size
            )
        }
    }

    suspend fun undo() {
        if (undoStack.isEmpty()) return

        val lastCommand = undoStack.last()

        lastCommand.undo()

        undoStack.removeLast()
        redoStack.add(lastCommand)

        telemetry.recordUndo(
            commandType = lastCommand.commandType,
            undoStackSize = undoStack.size,
            redoStackSize = redoStack.size
        )
    }

    suspend fun redo() {
        if (redoStack.isEmpty()) return

        val lastCommand = redoStack.last()

        lastCommand.redo()

        redoStack.removeLast()
        undoStack.add(lastCommand)

        telemetry.recordRedo(
            commandType = lastCommand.commandType,
            undoStackSize = undoStack.size,
            redoStackSize = redoStack.size
        )
    }
}
