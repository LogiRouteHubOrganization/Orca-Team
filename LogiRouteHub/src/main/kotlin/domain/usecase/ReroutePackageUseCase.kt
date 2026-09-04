package domain.usecase

import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.WarehouseRepository

class ReroutePackageUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(
        input: ReroutePackageRequest
    ): Package? {

        val warehouse = warehouseRepository.findById(
            input.warehouseId
        ) ?: return null

        val indexedPackage = warehouse.cargoQueue
            .withIndex()
            .find { indexedValue ->
                indexedValue.value.id == input.packageId
            } ?: return null

        val reroutedPackage = indexedPackage.value.copy(
            destinationWarehouse = input.newDestinationWarehouse
        )

        warehouse.updatePackageAtIndex(
            packageIndex = indexedPackage.index,
            logisticsPackage = reroutedPackage
        )

        warehouseRepository.update(warehouse)

        return reroutedPackage
    }
}
