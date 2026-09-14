package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.validator.ValidationResult
import domain.validator.VehicleValidator

class CreateVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        vehicle: Vehicle
    ): Result<Unit> {

        return when (val validation = VehicleValidator.validateCreate(vehicle)) {
            ValidationResult.Valid -> {
                vehicleRepository.add(vehicle)
                Result.success(Unit)
            }

            is ValidationResult.Invalid -> {
                Result.failure(
                    EntityValidationException(
                        validation.violations.joinToString("; ") {
                            "${it.field}: ${it.message}"
                        }
                    )
                )
            }
        }
    }
}