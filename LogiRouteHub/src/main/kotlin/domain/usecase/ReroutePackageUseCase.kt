package domain.usecase

import domain.model.Package
import domain.model.request.ReroutePackageRequest

class ReroutePackageUseCase {

    operator fun invoke(input: ReroutePackageRequest): Package? {

        val indexedPackage = input.warehouse.cargoQueue
            .withIndex()
            .find { indexedValue ->
                indexedValue.value.id == input.packageId
            } ?: return null

        val reroutedPackage = indexedPackage.value.copy(
            destinationWarehouse = input.newDestinationWarehouse
        )

        input.warehouse.updatePackageAtIndex(
            packageIndex = indexedPackage.index,
            logisticsPackage = reroutedPackage
        )

        return reroutedPackage
    }
}
