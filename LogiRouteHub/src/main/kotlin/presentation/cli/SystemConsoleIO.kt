package presentation.cli

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SystemConsoleIO(
    private val ioDispatcher: CoroutineDispatcher
) : ConsoleIO {

    override fun print(message: String) {
        kotlin.io.print(message)
    }

    override fun println(message: String) {
        kotlin.io.println(message)
    }

    override suspend fun readLine(): String? =
        withContext(ioDispatcher) {
            readlnOrNull()
        }
}