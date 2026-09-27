package domain.command

import domain.command.telemetry.CommandTelemetry

class CommandInvoker(
    private val telemetry: CommandTelemetry
) {
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()

    suspend fun execute(command: Command): Result<Unit> {
        return command.execute()
            .onSuccess {
                undoStack.add(command)
                redoStack.clear()

                telemetry.recordCommandExecution(
                    commandType = command.commandType,
                    undoStackSize = undoStack.size,
                    redoStackSize = redoStack.size
                )
            }
    }

    suspend fun undo(): Result<Unit> {
        val command = undoStack.lastOrNull()
            ?: return Result.success(Unit)

        return command.undo()
            .onSuccess {
                undoStack.removeLast()
                redoStack.add(command)

                telemetry.recordUndo(
                    commandType = command.commandType,
                    undoStackSize = undoStack.size,
                    redoStackSize = redoStack.size
                )
            }
    }

    suspend fun redo(): Result<Unit> {
        val command = redoStack.lastOrNull()
            ?: return Result.success(Unit)

        return command.redo()
            .onSuccess {
                redoStack.removeLast()
                undoStack.add(command)

                telemetry.recordRedo(
                    commandType = command.commandType,
                    undoStackSize = undoStack.size,
                    redoStackSize = redoStack.size
                )
            }
    }
}
