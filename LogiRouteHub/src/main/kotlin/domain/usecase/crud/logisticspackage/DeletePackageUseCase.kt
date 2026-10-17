package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.repository.PackageRepository
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator

class DeletePackageUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Unit> {

        val validationResult = IdValidator(
            field = "packageId",
            allowedPrefixes = setOf("PKG-")
        ).validate(id)

        return when (validationResult) {
            ResultValidator.Valid ->
                packageRepository.delete(id)

            is ResultValidator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

