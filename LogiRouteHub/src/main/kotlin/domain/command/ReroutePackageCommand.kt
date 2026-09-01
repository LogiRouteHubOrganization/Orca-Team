package domain.command

import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.WarehouseRepository
import domain.usecase.ReroutePackageUseCase


data class ReroutePackageCommandDependencies(
    val reroutePackageUseCase: ReroutePackageUseCase,
    val warehouseRepository: WarehouseRepository
)

class ReroutePackageCommand(
    private val dependencies: ReroutePackageCommandDependencies,
    private val input: ReroutePackageRequest
) : Command {

    private var previousPackageState: Package? = null
    private var previousIndex: Int? = null

    override fun execute(): Boolean {
        val warehouse = dependencies.warehouseRepository.findById(input.warehouseId)
            ?: return false

        val indexedPackage = warehouse.cargoQueue
            .withIndex()
            .find { it.value.id == input.packageId }
            ?: return false


        previousPackageState = indexedPackage.value.copy()
        previousIndex = indexedPackage.index


        val reroutedPackage = dependencies.reroutePackageUseCase(input)

        return reroutedPackage != null
    }

    override fun undo() {
        val warehouse = dependencies.warehouseRepository.findById(input.warehouseId)
            ?: return

        val oldPackage = previousPackageState ?: return
        val oldIndex = previousIndex ?: return


        warehouse.updatePackageAtIndex(
            packageIndex = oldIndex,
            logisticsPackage = oldPackage
        )

        dependencies.warehouseRepository.update(warehouse)
    }
}
