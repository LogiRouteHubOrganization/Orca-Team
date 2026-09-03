package domain.command

class CommandInvoker {
    private val undoStack = mutableListOf<Command>()
    private val redoStack = mutableListOf<Command>()

    fun execute(command: Command) {
        val isExecuted = command.execute()
        if (isExecuted) {
            undoStack.add(command)
            redoStack.clear()
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val lastCommand = undoStack.last()
        lastCommand.undo()
        undoStack.removeLast()
        redoStack.add(lastCommand)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val lastCommand = redoStack.last()
        lastCommand.redo()
        redoStack.removeLast()
        undoStack.add(lastCommand)

    }
}
