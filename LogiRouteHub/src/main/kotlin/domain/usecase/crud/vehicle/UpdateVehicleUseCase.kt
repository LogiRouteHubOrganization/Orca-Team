package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.vehicle.UpdateVehicleValidator

class UpdateVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val vehicleValidator: UpdateVehicleValidator
) {

    suspend operator fun invoke(
        id: String,
        updateRequest: VehicleUpdateModel
    ): Result<Vehicle> {

        val idValidationResult = IdValidator(
            field = "vehicleId",
            allowedPrefixes = setOf("TRK-")
        ).validate(id)

        if (idValidationResult is ValidationResult.Invalid) {
            return Result.failure(
                EntityValidationException(idValidationResult.errors)
            )
        }
        return when (
            val validationResult = vehicleValidator(updateRequest)
        ) {
            ValidationResult.Valid ->
                vehicleRepository.update(
                    id,
                    updateRequest
                )
            is ValidationResult.Invalid ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}

