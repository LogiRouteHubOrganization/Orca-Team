package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.model.Package
import domain.repository.PackageRepository
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator

class GetPackageByIdUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Package?> {

        return when ( val validationResult = IdValidator(
                field = "packageId",
                allowedPrefixes = setOf("PKG-")
            ).validate(id)
        ) {
            ResultValidator.Valid ->
                packageRepository.findById(id)

            is ResultValidator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

