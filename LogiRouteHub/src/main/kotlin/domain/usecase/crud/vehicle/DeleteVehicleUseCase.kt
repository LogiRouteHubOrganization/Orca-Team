package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.repository.VehicleRepository
import domain.validator.FieldViolation
import domain.validator.VehicleValidator

class DeleteVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        vehicleId: String
    ): Result<Unit> {

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
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Vehicle not found: $vehicleId"
                )
            )

        vehicleRepository.delete(vehicle.id)

        return Result.success(Unit)
    }
}