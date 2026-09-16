package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.model.Package
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase

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

    override suspend fun execute(): Boolean {
        val currentWarehouse =
            logisticsPackage.originWarehouse

        warehouse = currentWarehouse

        previousWarehouseQueue =
            currentWarehouse.cargoQueue.toList()

        dependencies.assignPackageToCargoQueueUseCase(logisticsPackage)

        afterWarehouseQueue =
            currentWarehouse.cargoQueue.toList()

        val isQueueChanged =
            currentWarehouse.cargoQueue != previousWarehouseQueue

        if (isQueueChanged) {
            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = createQueueState(previousWarehouseQueue),
                afterState = createQueueState(afterWarehouseQueue)
            )
        }

        return isQueueChanged
    }

    override suspend fun undo() {
        val executedWarehouse = warehouse ?: return

        val oldQueue = previousWarehouseQueue ?: return

        val newQueue = afterWarehouseQueue ?: return

        executedWarehouse.restoreCargoQueue(oldQueue)

        dependencies.warehouseRepository.update(
            executedWarehouse.id,
            WarehouseUpdateModel(
                name = executedWarehouse.name,
                regionalZone = executedWarehouse.regionalZone,
                latitude = executedWarehouse.latitude,
                longitude = executedWarehouse.longitude
            )
        )

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createQueueState(newQueue),
            afterState = createQueueState(oldQueue)
        )
    }

    override suspend fun redo() {
        val executedWarehouse = warehouse ?: return

        val oldQueue = previousWarehouseQueue ?: return

        val newQueue = afterWarehouseQueue ?: return

        executedWarehouse.restoreCargoQueue(newQueue)

        dependencies.warehouseRepository.update(
            executedWarehouse.id,
            WarehouseUpdateModel(
                name = executedWarehouse.name,
                regionalZone = executedWarehouse.regionalZone,
                latitude = executedWarehouse.latitude,
                longitude = executedWarehouse.longitude
            )
        )

        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = createQueueState(oldQueue),
            afterState = createQueueState(newQueue)
        )
    }

    private fun createQueueState(
        queue: List<Package>?
    ): String {
        return "Queue count: ${queue?.size}\n" +
                "First packages: ${queue?.take(5)?.map { it.id }}"
    }
}
