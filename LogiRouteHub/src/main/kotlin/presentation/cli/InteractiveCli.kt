package presentation.cli

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class InteractiveCli(
    private val routeCommand: RouteCommand,
    private val classificationCommand: ClassificationCommand,
    private val fleetTrackingCommand: FleetTrackingCommand,
    private val console: ConsoleIO
) {

    suspend fun start() = coroutineScope {
        try {
            while (true) {
                showMenu()

                val choice = console.readLine()

                if (choice == "4" || choice == null) {
                    break
                }

                try {
                    executeCommand(choice, this)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    console.println("Command failed: ${error.message}")
                }
            }
        } finally {
            withContext(NonCancellable) {
                fleetTrackingCommand.stop()
            }
        }

        console.println("Goodbye!")
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
                console.println("Fleet tracking stopped.")
            }
            else -> console.println("Invalid choice.")
        }
    }

    private fun showMenu() {
        console.println()
        console.println("========== LogiRouteHub ==========")
        console.println("1. Calculate Route")
        console.println("2. Classify Package Sector")
        console.println("3. Start Fleet Tracking")
        console.println("4. Exit")
        console.println("5. Stop Fleet Tracking")
        console.print("Enter your choice: ")
    }
}