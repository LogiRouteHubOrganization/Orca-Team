package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.validator.FieldViolation
import domain.validator.VehicleValidator

class GetVehicleByIdUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        vehicleId: String
    ): Result<Vehicle> {

        val violations = mutableListOf<FieldViolation>()

        VehicleValidator.validateId(
            id = vehicleId,
            violations = violations
        )

        if (violations.isNotEmpty()) {
            return Result.failure(
                EntityValidationException(
                    violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val vehicle = vehicleRepository.findById(vehicleId)

        return if (vehicle != null) {
            Result.success(vehicle)
        } else {
            Result.failure(
                ResourceNotFoundException(
                    "Vehicle not found: $vehicleId"
                )
            )
        }
    }
}