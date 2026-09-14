package domain.command

interface Command {
    val commandType: String

    suspend fun execute(): Boolean

    suspend fun undo()

    suspend fun redo()
}