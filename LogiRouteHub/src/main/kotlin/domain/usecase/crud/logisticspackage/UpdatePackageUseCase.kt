package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.validator.ResultValidator
import domain.validator.logisticspackage.UpdatePackageValidator
import domain.validator.rule.IdValidator

class UpdatePackageUseCase(
    private val packageRepository: PackageRepository,
    private val packageValidator: UpdatePackageValidator
) {
    suspend operator fun invoke(
        id: String,
        updateRequest: PackageUpdateModel
    ): Result<Package> {

        val idValidationResult = IdValidator(
            field = "packageId",
            allowedPrefixes = setOf("PKG-")
        ).validate(id)

        if (idValidationResult is ResultValidator.Invalid) {
            return Result.failure(
                EntityValidationException(idValidationResult.errors)
            )
        }

        return when (
            val validationResult = packageValidator(updateRequest)
        ) {
            ResultValidator.Valid ->
                packageRepository.update(id, updateRequest)

            is ResultValidator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )

            else ->
                Result.failure(
                    IllegalArgumentException("Unhandled validation result state")
                )
        }
    }
}
