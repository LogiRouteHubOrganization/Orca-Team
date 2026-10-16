package presentation.cli


import domain.usecase.FindOptimalPathUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteCommand(
    private val findOptimalPathUseCase: FindOptimalPathUseCase
) {

    suspend fun execute() = coroutineScope {
        println("\n===== Route Calculation =====")

        val originId = readInput("Enter origin warehouse ID: ")
            ?: return@coroutineScope

        val destinationId = readInput("Enter destination warehouse ID: ")
            ?: return@coroutineScope

        val loadingJob = launch {
            for (step in 1..100) {
                print(".")
                delay(300)
            }
        }

        try {
            findOptimalPathUseCase(
                startWarehouseId = originId,
                destinationWarehouseId = destinationId
            ).onSuccess { path ->
                println()
                println(
                    "Optimal Path: " +
                            path.joinToString(" -> ") { it.id }
                )
            }.onFailure { error ->
                println("\nRoute failed: ${error.message}")
            }
        } finally {
            loadingJob.cancelAndJoin()
        }
    }

    private suspend fun readInput(message: String): String? {
        print(message)

        return withContext(Dispatchers.IO) {
            readlnOrNull()
        }
    }
}