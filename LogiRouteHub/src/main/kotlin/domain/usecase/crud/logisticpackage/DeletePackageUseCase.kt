package domain.usecase.crud.logisticpackage

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.repository.PackageRepository
import domain.validator.FieldViolation
import domain.validator.PackageValidator

class DeletePackageUseCase(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(
        packageId: String
    ): Result<Unit> {

        val violations = mutableListOf<FieldViolation>()

        PackageValidator.validateId(
            id = packageId,
            violations = violations
        )

        if (violations.isNotEmpty()) {
            return Result.failure(
                EntityValidationException(
                    violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val logisticsPackage = packageRepository.findById(packageId)
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Package not found: $packageId"
                )
            )

        packageRepository.delete(logisticsPackage.id)

        return Result.success(Unit)
    }
}