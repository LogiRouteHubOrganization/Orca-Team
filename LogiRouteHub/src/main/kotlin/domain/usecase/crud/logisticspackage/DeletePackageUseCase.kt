
package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.repository.PackageRepository
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator

class DeletePackageUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Unit> {

        return when (
            val validationResult = IdValidator(
                field = "packageId",
                allowedPrefixes = setOf("PKG-")
            ).validate(id)
        ) {
            ValidationResult.Valid ->
                packageRepository.delete(id)

            is ValidationResult.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
