package domain.command

import domain.algorithm.sorting.PackageQuickSorter
import domain.model.Package
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase

data class AssignPackageToCargoQueueCommandDependencies(
    val assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase,
    val warehouseRepository: WarehouseRepository
)

class AssignPackageToCargoQueueCommand(
    private val dependencies: AssignPackageToCargoQueueCommandDependencies,
    private val logisticsPackage: Package
) : Command {

    private var previousWarehouseQueue: List<Package>? = null

    override fun execute(): Boolean {
        val originWarehouse = logisticsPackage.originWarehouse

        // Snapshot state before mutation for undo
        previousWarehouseQueue = originWarehouse.cargoQueue.toList()

        dependencies.assignPackageToCargoQueueUseCase(logisticsPackage)

        return true
    }

    override fun undo() {
        val oldQueue = previousWarehouseQueue ?: return
        val originWarehouse = logisticsPackage.originWarehouse

        originWarehouse.restoreCargoQueue(oldQueue)
        dependencies.warehouseRepository.update(originWarehouse)
    }
}
