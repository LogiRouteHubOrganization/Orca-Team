
package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.model.Package
import domain.repository.PackageRepository
import domain.validator.ValidationResult
import domain.validator.logisticspackage.CreatePackageValidator

class CreatePackageUseCase(
    private val packageRepository: PackageRepository,
    private val packageValidator: CreatePackageValidator
) {
    suspend operator fun invoke(
        logisticsPackage: Package
    ): Result<Package> {

        return when (
            val validationResult = packageValidator(
                id = logisticsPackage.id,
                weightByKg = logisticsPackage.weightByKg,
                priority = logisticsPackage.priority ,
                originWarehouse = logisticsPackage.originWarehouse,
                destinationWarehouse = logisticsPackage.destinationWarehouse
            )
        ) {
            ValidationResult.Success ->
                packageRepository.add(logisticsPackage)

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

