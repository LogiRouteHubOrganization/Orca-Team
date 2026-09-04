package domain.validation

import domain.command.Command
import domain.command.CommandInvoker
import domain.command.telemetry.ConsoleTelemetry
data class CommandHistory(
    val AssignPackageToQueueCommand: Command,
    val DispatchVehicleCommand: Command,
    val ReroutePackageCommand: Command
)

class CommandHistoryValidation {

    fun run(history: CommandHistory) {

        println("=== Command History Validation ===")

        val invoker = CommandInvoker(ConsoleTelemetry())

        invoker.execute(history.AssignPackageToQueueCommand)
        invoker.execute(history.ReroutePackageCommand)

        invoker.undo()
        invoker.redo()

        invoker.execute(history.DispatchVehicleCommand)

        invoker.undo()
    }
}