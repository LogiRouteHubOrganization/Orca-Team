package domain.command

import domain.model.Package
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.command.telemetry.CommandTelemetry

data class AssignPackageToQueueCommandDependencies(
    val assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase,
    val warehouseRepository: WarehouseRepository,
    val telemetry: CommandTelemetry
)

class AssignPackageToQueueCommand(
    private val logisticsPackage: Package,
    private val dependencies: AssignPackageToQueueCommandDependencies
) : Command {

    private var warehouse: Warehouse? = null
    private var previousWarehouseQueue: List<Package>? = null
    private var afterWarehouseQueue: List<Package>? = null

    override val commandType = "AssignPackageToQueueCommand"

    override fun execute(): Boolean {
        val currentWarehouse = logisticsPackage.originWarehouse

        warehouse = currentWarehouse
        previousWarehouseQueue = currentWarehouse.cargoQueue.toList()

        dependencies.assignPackageToCargoQueueUseCase(logisticsPackage)

        afterWarehouseQueue = currentWarehouse.cargoQueue.toList()

        val isQueueChanged = currentWarehouse.cargoQueue != previousWarehouseQueue

        if (isQueueChanged) {
            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = previousWarehouseQueue.toString(),
                afterState = afterWarehouseQueue.toString()
            )
        }
        return isQueueChanged
    }

    override fun undo() {
        val executedWarehouse = warehouse ?: return
        val oldQueue = previousWarehouseQueue ?: return
        val newQueue = afterWarehouseQueue ?: return

        executedWarehouse.restoreCargoQueue(oldQueue)
        dependencies.warehouseRepository.update(executedWarehouse)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = newQueue.toString(),
            afterState = oldQueue.toString()
        )
    }

    override fun redo() {
        val executedWarehouse = warehouse ?: return
        val oldQueue = previousWarehouseQueue ?: return
        val newQueue = afterWarehouseQueue ?: return

        executedWarehouse.restoreCargoQueue(newQueue)
        dependencies.warehouseRepository.update(executedWarehouse)

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = oldQueue.toString(),
            afterState = newQueue.toString()
        )
    }
}
