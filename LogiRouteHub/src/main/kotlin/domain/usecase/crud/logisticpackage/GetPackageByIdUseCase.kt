package domain.usecase.crud.logisticpackage

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Package
import domain.repository.PackageRepository
import domain.validator.FieldViolation
import domain.validator.PackageValidator

class GetPackageByIdUseCase(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(
        packageId: String
    ): Result<Package> {

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

        return if (logisticsPackage != null) {
            Result.success(logisticsPackage)
        } else {
            Result.failure(
                ResourceNotFoundException(
                    "Package not found: $packageId"
                )
            )
        }
    }
}