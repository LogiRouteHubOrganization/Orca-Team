package domain.command

import domain.command.telemetry.CommandTelemetry
import domain.command.telemetry.QueueState
import domain.extension.runCatchingCancellable
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

    override suspend fun execute(): Result<Unit> {
        return runCatching  {
            val currentWarehouse = logisticsPackage.originWarehouse

            warehouse = currentWarehouse

            previousWarehouseQueue = currentWarehouse.cargoQueue.toList()

            dependencies.assignPackageToCargoQueueUseCase(logisticsPackage).getOrThrow()

            afterWarehouseQueue = currentWarehouse.cargoQueue.toList()
            recordStateTransition()

            Unit
        }
    }

    override suspend fun undo(): Result<Unit> {
        return runCatchingCancellable  {
            val executedWarehouse = warehouse ?: error("Command has not been executed")

            val oldQueue = previousWarehouseQueue
                    ?: error("Previous state is unavailable")

            val newQueue = afterWarehouseQueue
                    ?: error("After state is unavailable")

            executedWarehouse.restoreCargoQueue(oldQueue)

            dependencies.warehouseRepository.update(
                executedWarehouse.id,
                WarehouseUpdateModel(
                    name = executedWarehouse.name,
                    regionalZone = executedWarehouse.regionalZone,
                    latitude = executedWarehouse.latitude,
                    longitude = executedWarehouse.longitude
                )
            ).getOrThrow()

            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = QueueState(
                    newQueue.map { it.id }
                ),
                afterState = QueueState(
                    oldQueue.map { it.id }
                )
            )

            Unit
        }
    }

    override suspend fun redo(): Result<Unit> {
        return runCatchingCancellable  {
            val executedWarehouse = warehouse ?: error("Command has not been executed")

            val oldQueue = previousWarehouseQueue ?: error("Previous state is unavailable")

            val newQueue = afterWarehouseQueue ?: error("After state is unavailable")

            executedWarehouse.restoreCargoQueue(newQueue)

            dependencies.warehouseRepository.update(
                executedWarehouse.id,
                WarehouseUpdateModel(
                    name = executedWarehouse.name,
                    regionalZone = executedWarehouse.regionalZone,
                    latitude = executedWarehouse.latitude,
                    longitude = executedWarehouse.longitude
                )
            ).getOrThrow()

            dependencies.telemetry.recordStateTransition(
                commandType = commandType,
                beforeState = QueueState(
                    oldQueue.map { it.id }
                ),
                afterState = QueueState(
                    newQueue.map { it.id }
                )
            )

            Unit
        }
    }

    private fun recordStateTransition() {
        dependencies.telemetry.recordStateTransition(
            commandType = commandType,
            beforeState = QueueState(
                previousWarehouseQueue.orEmpty().map { it.id }
            ),
            afterState = QueueState(
                afterWarehouseQueue.orEmpty().map { it.id }
            )
        )
    }
}
