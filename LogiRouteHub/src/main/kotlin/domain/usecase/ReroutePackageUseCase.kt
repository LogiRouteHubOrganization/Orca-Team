package domain.usecase

import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.model.updateinput.PackageUpdateModel
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

        val packageUpdateModel = PackageUpdateModel(
            destinationWarehouseId = input.newDestinationWarehouse.id
        )

        packageRepository.update(reroutedPackage.id, packageUpdateModel)

        return packageRepository.findById(reroutedPackage.id)
    }
}
