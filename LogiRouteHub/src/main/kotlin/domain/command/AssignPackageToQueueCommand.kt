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
    private val dependencies: AssignPackageToQueueCommandDependencies,
) : Command {
    private var warehouse: Warehouse? = null
    private var previousWarehouseQueue: List<Package>? = null

    override fun execute(): Boolean {

        return true
    }

    override fun undo() {

    }
}
