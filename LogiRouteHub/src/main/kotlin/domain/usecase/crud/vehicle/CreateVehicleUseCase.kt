package domain.usecase.crud.vehicle

import domain.exception.EntityValidationException
import domain.model.Vehicle
import domain.repository.VehicleRepository
import domain.validator.ValidationResult
import domain.validator.vehicle.CreateVehicleValidator

class CreateVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val vehicleValidator: CreateVehicleValidator
) {

    suspend operator fun invoke(
        vehicle: Vehicle
    ): Result<Vehicle> {

        return when (
            val validationResult = vehicleValidator(
                id = vehicle.id,
                maxCapacityKg = vehicle.maxCapacityKg,
                costPerKm = vehicle.costPerKm,
                currentWarehouse = vehicle.currentWarehouse
            )
        ) {
            ValidationResult.Success ->
                vehicleRepository.add(vehicle)

            is ValidationResult.Failure ->
                Result.failure(
                    EntityValidationException(validationResult.errors)
                )
        }
    }
}