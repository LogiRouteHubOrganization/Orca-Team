package domain.command

import domain.model.Package
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase

data class AssignPackageToQueueCommandDependencies(
    val assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase,
    val warehouseRepository: WarehouseRepository
)

class AssignPackageToQueueCommand(
    private val logisticsPackage: Package,
    private val dependencies: AssignPackageToQueueCommandDependencies
) : Command {

    private var warehouse: Warehouse? = null
    private var previousWarehouseQueue: List<Package>? = null

    override fun execute(): Boolean {
        val currentWarehouse = logisticsPackage.originWarehouse

        warehouse = currentWarehouse
        previousWarehouseQueue = currentWarehouse.cargoQueue.toList()

        dependencies.assignPackageToCargoQueueUseCase(logisticsPackage)

        return currentWarehouse.cargoQueue != previousWarehouseQueue
    }

    override fun undo() {
        val executedWarehouse = warehouse ?: return
        val oldQueue = previousWarehouseQueue ?: return

        executedWarehouse.restoreCargoQueue(oldQueue)
        dependencies.warehouseRepository.update(executedWarehouse)
    }

    override fun redo() {
        if (warehouse == null || previousWarehouseQueue == null) return

        dependencies.assignPackageToCargoQueueUseCase(logisticsPackage)
    }
}