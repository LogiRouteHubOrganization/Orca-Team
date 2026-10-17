package presentation.cli

import core.dispatcher.AppDispatchers
import domain.usecase.FindOptimalPathUseCase
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class RouteCommand(
    private val findOptimalPathUseCase: FindOptimalPathUseCase,
    private val console: ConsoleIO,
    private val dispatchers: AppDispatchers
) {

    suspend fun execute() = coroutineScope {
        console.println("\n===== Route Calculation =====")

        val originId = readInput("Enter origin warehouse ID: ")
            ?: return@coroutineScope

        val destinationId = readInput("Enter destination warehouse ID: ")
            ?: return@coroutineScope

        val loadingJob = launch {
            while (isActive) {
                console.print(".")
                delay(300)
            }
        }

        try {
            val result = withContext(dispatchers.computation) {
                findOptimalPathUseCase(
                    startWarehouseId = originId,
                    destinationWarehouseId = destinationId
                )
            }

            result
                .onSuccess { path ->
                    console.println()
                    console.println(
                        "Optimal Path: " +
                                path.joinToString(" -> ") { it.id }
                    )
                }
                .onFailure { error ->
                    console.println()
                    console.println("Route failed: ${error.message}")
                }
        } finally {
            loadingJob.cancelAndJoin()
            console.println()
        }
    }

    private suspend fun readInput(message: String): String? {
        console.print(message)
        return console.readLine()
    }
}