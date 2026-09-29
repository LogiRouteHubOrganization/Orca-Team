package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.repository.VehicleRepository
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator

class DeleteVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Unit> {

        return when (
            val validationResult = IdValidator(
                field = "vehicleId",
                allowedPrefixes = setOf("TRK-")
            ).validate(id)
        ) {
            ResultValidator.Valid ->
                vehicleRepository.delete(id)

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
