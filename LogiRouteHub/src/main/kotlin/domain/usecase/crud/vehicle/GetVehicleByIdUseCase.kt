package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.validator.ResultVaildator
import domain.validator.rule.IdValidator

class GetVehicleByIdUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Vehicle?> {

        return when (
            val validationResult = IdValidator(
                field = "vehicleId",
                allowedPrefixes = setOf("TRK-")
            ).validate(id)
        ) {
            ResultVaildator.Valid ->
                vehicleRepository.findById(id)

            is ResultVaildator.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}
