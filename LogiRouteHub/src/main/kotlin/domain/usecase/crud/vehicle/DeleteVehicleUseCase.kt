package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.repository.VehicleRepository
import domain.validator.ResultVaildator
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
            ResultVaildator.Valid ->
                vehicleRepository.delete(id)

            is ResultVaildator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
