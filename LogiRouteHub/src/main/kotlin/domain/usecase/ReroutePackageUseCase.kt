package domain.usecase

import domain.exception.EntityValidationException
import domain.extension.runCatchingCancellable
import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository
import domain.validator.ResultValidator
import domain.validator.logisticspackage.UpdatePackageValidator

class ReroutePackageUseCase(
    private val packageRepository: PackageRepository,
    private val warehouseRepository: WarehouseRepository,
    private val updatePackageValidator: UpdatePackageValidator
) {

    suspend operator fun invoke(
        input: ReroutePackageRequest
    ): Result<Package?> = runCatchingCancellable {

        val warehouse = warehouseRepository
            .findById(input.warehouseId)
            .getOrThrow()
            ?: return@runCatchingCancellable null

        val logisticsPackage = warehouse.cargoQueue.find {
            it.id == input.packageId
        } ?: return@runCatchingCancellable null

        val reroutedPackage = logisticsPackage.copy(
            destinationWarehouse = input.newDestinationWarehouse
        )

        val packageUpdateModel = PackageUpdateModel(
            destinationWarehouseId = input.newDestinationWarehouse.id
        )

        val validationResult =
            updatePackageValidator(packageUpdateModel)

        if (validationResult is ResultValidator.Invalid) {
            throw EntityValidationException(validationResult.errors)
        }

        packageRepository
            .update(reroutedPackage.id, packageUpdateModel)
            .getOrThrow()
    }
}
