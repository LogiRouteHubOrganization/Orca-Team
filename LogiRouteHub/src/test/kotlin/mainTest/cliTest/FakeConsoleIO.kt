package mainTest.presentationTest.cliTest

import presentation.cli.ConsoleIO

class FakeConsoleIO(
    inputs: List<String?>
) : ConsoleIO {

    private val inputQueue = inputs.toMutableList()
    private var nextInputIndex = 0
    private val output = StringBuilder()

    override fun print(message: String) {
        output.append(message)
    }

    override fun println(message: String) {
        output.append(message).append('\n')
    }

    override suspend fun readLine(): String? {
        if (nextInputIndex >= inputQueue.size) {
            return null
        }

        return inputQueue[nextInputIndex++]
    }

    fun outputText(): String = output.toString()
}