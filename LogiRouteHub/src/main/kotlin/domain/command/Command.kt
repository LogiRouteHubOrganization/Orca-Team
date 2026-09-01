package domain.command

interface Command {
    fun execute(): Boolean
    fun undo()
}
