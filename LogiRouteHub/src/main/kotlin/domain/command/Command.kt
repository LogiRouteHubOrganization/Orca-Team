package domain.command

interface Command {
    val commandType: String

    suspend fun execute(): Result<Unit>

    suspend fun undo(): Result<Unit>

    suspend fun redo(): Result<Unit>
}