package presentation.cli

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class InteractiveCli(
    private val routeCommand: RouteCommand,
    private val classificationCommand: ClassificationCommand,
    private val fleetTrackingCommand: FleetTrackingCommand
) {

    suspend fun start() = coroutineScope {
        try {
            for (command in generateSequence { true }) {
                showMenu()

                val choice = readChoice()

                if (choice == "4" || choice == null) {
                    break
                }

                try {
                    executeCommand(choice, this)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    println("Command failed: ${error.message}")
                }
            }
        } finally {
            withContext(NonCancellable) {
                fleetTrackingCommand.stop()
            }
        }

        println("Goodbye!")
    }

    private suspend fun executeCommand(
        choice: String,
        scope: CoroutineScope
    ) {
        when (choice) {
            "1" -> routeCommand.execute()
            "2" -> classificationCommand.execute()
            "3" -> fleetTrackingCommand.start(scope)
            "5" -> {
                fleetTrackingCommand.stop()
                println("Fleet tracking stopped.")
            }
            else -> println("Invalid choice.")
        }
    }

    private suspend fun readChoice(): String? =
        withContext(Dispatchers.IO) {
            readlnOrNull()
        }

    private fun showMenu() {
        println()
        println("========== LogiRouteHub ==========")
        println("1. Calculate Route")
        println("2. Classify Package Sector")
        println("3. Start Fleet Tracking")
        println("4. Exit")
        println("5. Stop Fleet Tracking")
        print("Enter your choice: ")
    }
}