package domain.command

interface Command {
    val commandType: String
    fun execute(): Boolean
    fun undo()
    fun redo()
}
