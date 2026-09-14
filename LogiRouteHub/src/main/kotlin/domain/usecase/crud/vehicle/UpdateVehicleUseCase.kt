package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.exception.ResourceNotFoundException
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.validator.ValidationResult
import domain.validator.VehicleValidator

class UpdateVehicleUseCase(
    private val vehicleRepository: VehicleRepository
) {

    suspend operator fun invoke(
        id: String,
        maxCapacityKg: Double? = null,
        costPerKm: Double? = null
    ): Result<Unit> {

        val validation = VehicleValidator.validateUpdate(
            id = id,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm
        )

        if (validation is ValidationResult.Invalid) {
            return Result.failure(
                EntityValidationException(
                    validation.violations.joinToString("; ") {
                        "${it.field}: ${it.message}"
                    }
                )
            )
        }

        val currentVehicle = vehicleRepository.findById(id)
            ?: return Result.failure(
                ResourceNotFoundException(
                    "Vehicle not found: $id"
                )
            )

        val updatedVehicle = Vehicle(
            id = currentVehicle.id,
            maxCapacityKg = maxCapacityKg ?: currentVehicle.maxCapacityKg,
            costPerKm = costPerKm ?: currentVehicle.costPerKm,
            currentWarehouse = currentVehicle.currentWarehouse
        )

        updatedVehicle.restoreAssignedPackages(
            currentVehicle.assignedPackages
        )

        vehicleRepository.update(updatedVehicle)

        return Result.success(Unit)
    }
}