package domain.usecase

import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class ReroutePackageUseCase(
    private val packageRepository: PackageRepository,
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        input: ReroutePackageRequest
    ): Package? {

        val warehouse = warehouseRepository.findById(
            input.warehouseId
        ) ?: return null

        val logisticsPackage = warehouse.cargoQueue.find {
            it.id == input.packageId
        } ?: return null

        val reroutedPackage = logisticsPackage.copy(
            destinationWarehouse = input.newDestinationWarehouse
        )

        packageRepository.update(reroutedPackage)

        return reroutedPackage
    }
}
