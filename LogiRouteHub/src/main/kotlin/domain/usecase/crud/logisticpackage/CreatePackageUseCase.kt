package domain.usecase.crud.logisticpackage

import domain.exception.EntityValidationException
import domain.model.Package
import domain.repository.PackageRepository
import domain.validator.PackageValidator
import domain.validator.ValidationResult

class CreatePackageUseCase(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(
        logisticsPackage: Package
    ): Result<Unit> {

        return when (
            val validation = PackageValidator.validateCreate(logisticsPackage)
        ) {
            ValidationResult.Valid -> {
                packageRepository.add(logisticsPackage)
                Result.success(Unit)
            }

            is ValidationResult.Invalid -> {
                Result.failure(
                    EntityValidationException(
                        validation.violations.joinToString("; ") {
                            "${it.field}: ${it.message}"
                        }
                    )
                )
            }
        }
    }
}