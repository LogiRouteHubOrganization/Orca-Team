package domain.command

import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.WarehouseRepository
import domain.usecase.ReroutePackageUseCase

class ReroutePackageCommand(
    private val useCase: ReroutePackageUseCase,
    private val warehouseRepository: WarehouseRepository,
    private val input: ReroutePackageRequest
) : Command {

    private var previousPackage: Package? = null
    private var previousIndex: Int? = null

    override fun execute(): Boolean {

        val warehouse = warehouseRepository.findById(input.warehouseId)
            ?: return false

        val indexedPackage = warehouse.cargoQueue
            .withIndex()
            .find { it.value.id == input.packageId }
            ?: return false

        previousPackage = indexedPackage.value
        previousIndex = indexedPackage.index

        val reroutedPackage = useCase(input)

        return reroutedPackage != null
    }

    override fun undo() {

        val warehouse = warehouseRepository.findById(input.warehouseId)
            ?: return

        val oldPackage = previousPackage
            ?: return

        val oldIndex = previousIndex
            ?: return

        warehouse.updatePackageAtIndex(
            packageIndex = oldIndex,
            logisticsPackage = oldPackage
        )

        warehouseRepository.update(warehouse)
    }
}