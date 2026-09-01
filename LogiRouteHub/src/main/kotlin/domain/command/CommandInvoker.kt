package domain.command

class CommandInvoker {
    private val history = mutableListOf<Command>()

    fun execute(command: Command) {
        val isExecuted = command.execute()
        if (isExecuted) {
            history.add(command)
        }
    }

    fun undo() {
        if (history.isEmpty()) return
        val lastCommand = history.last()
        lastCommand.undo()
        history.removeLast()
    }
}
