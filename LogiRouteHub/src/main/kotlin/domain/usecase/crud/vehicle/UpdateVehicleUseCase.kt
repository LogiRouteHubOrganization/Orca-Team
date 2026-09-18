package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.repository.VehicleRepository
import domain.validator.ValidationResult
import domain.validator.vehicle.UpdateVehicleValidator

class UpdateVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val vehicleValidator: UpdateVehicleValidator
) {

    suspend operator fun invoke(
        id: String,
        updateRequest: VehicleUpdateModel
    ): Result<Vehicle> {

        return when (
            val validationResult = vehicleValidator(updateRequest)
        ) {
            ValidationResult.Success ->
                vehicleRepository.update(
                    id,
                    updateRequest
                )

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}