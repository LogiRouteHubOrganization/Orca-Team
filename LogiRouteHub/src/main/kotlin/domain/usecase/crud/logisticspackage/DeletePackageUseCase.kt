
package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.repository.PackageRepository
import domain.validator.ResultVaildator
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
            ResultVaildator.Valid ->
                packageRepository.delete(id)

            is ResultVaildator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
