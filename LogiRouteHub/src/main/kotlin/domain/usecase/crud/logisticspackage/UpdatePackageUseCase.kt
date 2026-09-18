
package domain.usecase.crud.logisticspackage

import domain.exception.EntityValidationException
import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.repository.PackageRepository
import domain.validator.ValidationResult
import domain.validator.logisticspackage.UpdatePackageValidator

class UpdatePackageUseCase(
    private val packageRepository: PackageRepository,
    private val packageValidator: UpdatePackageValidator
) {
    suspend operator fun invoke(
        id: String,
        updateRequest: PackageUpdateModel
    ): Result<Package> {

        return when (
            val validationResult = packageValidator(updateRequest)
        ) {
            ValidationResult.Success ->
                packageRepository.update(id, updateRequest)

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

