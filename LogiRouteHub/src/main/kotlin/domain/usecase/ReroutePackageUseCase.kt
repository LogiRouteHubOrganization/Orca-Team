package domain.usecase

import domain.exception.EntityValidationException
import domain.model.Package
import domain.model.request.ReroutePackageRequest
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository
import domain.validator.ValidationResult
import domain.validator.logisticspackage.UpdatePackageValidator

class ReroutePackageUseCase(
    private val packageRepository: PackageRepository,
    private val warehouseRepository: WarehouseRepository,
    private val updatePackageValidator: UpdatePackageValidator
) {

    suspend operator fun invoke(
        input: ReroutePackageRequest
    ): Result<Package?> = runCatching {

        val warehouse = warehouseRepository
            .findById(input.warehouseId)
            .getOrThrow()
            ?: return@runCatching null

        val logisticsPackage = warehouse.cargoQueue.find {
            it.id == input.packageId
        } ?: return@runCatching null

        val reroutedPackage = logisticsPackage.copy(
            destinationWarehouse = input.newDestinationWarehouse
        )

        val packageUpdateModel = PackageUpdateModel(
            destinationWarehouseId = input.newDestinationWarehouse.id
        )

        val validationResult =
            updatePackageValidator(packageUpdateModel)

        if (validationResult is ValidationResult.Failure) {
            throw EntityValidationException(validationResult.errors)
        }

        packageRepository
            .update(reroutedPackage.id, packageUpdateModel)
            .getOrThrow()
    }
}
