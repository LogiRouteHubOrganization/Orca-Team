package domain.usecase

import domain.model.Package
import domain.model.Warehouse

class ReroutePackageUseCase {

    operator fun invoke(
        warehouse: Warehouse,
        packageId: String,
        newDestinationWarehouse: Warehouse
    ): Package? {

        val indexedPackage = warehouse.cargoQueue
            .withIndex()
            .find { indexedValue -> indexedValue.value.id == packageId } ?: return null

        val reroutedPackage = indexedPackage.value.copy(
            destinationWarehouse = newDestinationWarehouse
        )
        warehouse.updatePackageAtIndex(
            packageIndex = indexedPackage.index,
            logisticsPackage = reroutedPackage
        )
        return reroutedPackage
    }
}