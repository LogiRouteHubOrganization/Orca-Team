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
    private val dependencies: AssignPackageToQueueCommandDependencies,
) : Command {

    private var warehouse: Warehouse? = null
    private var previousWarehouseQueue: List<Package>? = null

    override fun execute(): Boolean {

        warehouse = logisticsPackage.originWarehouse
        previousWarehouseQueue = warehouse?.cargoQueue?.toList()

        dependencies.assignPackageToCargoQueueUseCase(logisticsPackage)

        return true
    }

    override fun undo() {

        val currentWarehouse = warehouse ?: return
        val previousQueue = previousWarehouseQueue ?: return

        currentWarehouse.restoreCargoQueue(previousQueue)
        dependencies.warehouseRepository.update(currentWarehouse)
    }
}
