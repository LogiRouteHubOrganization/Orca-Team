
package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.model.Package
import domain.repository.PackageRepository
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator

class GetPackageByIdUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Package?> {

        return when (
            val validationResult = IdValidator(
                field = "packageId",
                allowedPrefixes = setOf("PKG-")
            ).validate(id)
        ) {
            ValidationResult.Valid ->
                packageRepository.findById(id)

            is ValidationResult.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}


