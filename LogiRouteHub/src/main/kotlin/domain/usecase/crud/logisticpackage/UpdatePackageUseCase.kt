package domain.usecase.crud.logisticpackage

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Package
import domain.repository.PackageRepository
import domain.validator.PackageValidator
import domain.validator.ValidationResult

class UpdatePackageUseCase(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(
        id: String,
        weightByKg: Double? = null,
        priority: String? = null
    ): Result<Unit> {

        val validation = PackageValidator.validateUpdate(
            id = id,
            weightByKg = weightByKg,
            priority = priority
        )

        if (validation is ValidationResult.Invalid) {
            return Result.failure(
                EntityValidationException(
                    validation.violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val currentPackage = packageRepository.findById(id)
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Package not found: $id"
                )
            )

        val updatedPackage = Package(
            id = currentPackage.id,
            weightByKg = weightByKg ?: currentPackage.weightByKg,
            priority = priority ?: currentPackage.priority,
            originWarehouse = currentPackage.originWarehouse,
            destinationWarehouse = currentPackage.destinationWarehouse
        )

        packageRepository.update(updatedPackage)

        return Result.success(Unit)
    }
}