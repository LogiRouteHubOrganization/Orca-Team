package presentation.cli

interface ConsoleIO {
    fun print(message: String)
    fun println(message: String = "")
    suspend fun readLine(): String?
}