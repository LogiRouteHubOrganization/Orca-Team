package domain.validator.vehicle

import domain.model.Vehicle
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreateVehicleValidator {

    operator fun invoke(vehicle: Vehicle): ValidationResult {

        val results = listOf(
            CommonValidators.validateId(
                field = "id",
                value = vehicle.id,
                allowedPrefixes = setOf("TRK-")
            ),

            CommonValidators.validatePositiveNumber(
                field = "maxCapacityKg",
                value = vehicle.maxCapacityKg
            ),

            CommonValidators.validatePositiveNumber(
                field = "costPerKm",
                value = vehicle.costPerKm
            ),

            CommonValidators.validateId(
                field = "currentWarehouseId",
                value = vehicle.currentWarehouse.id,
                allowedPrefixes = setOf("WH-")
            )
        )

        val errors = results
            .filterIsInstance<ValidationResult.Failure>()
            .flatMap { it.errors }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }
}