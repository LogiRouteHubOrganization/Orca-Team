package domain.validator.vehicle

import domain.model.Vehicle
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreateVehicleValidator {

    operator fun invoke(vehicle: Vehicle): ValidationResult {

        val errors = mutableListOf<ValidationError>()

        collectErrors(
            CommonValidators.validateId(
                field = "id",
                value = vehicle.id,
                allowedPrefixes = setOf("TRK-")
            ),
            errors
        )

        collectErrors(
            CommonValidators.validatePositiveNumber(
                field = "maxCapacityKg",
                value = vehicle.maxCapacityKg
            ),
            errors
        )

        collectErrors(
            CommonValidators.validatePositiveNumber(
                field = "costPerKm",
                value = vehicle.costPerKm
            ),
            errors
        )

        collectErrors(
            CommonValidators.validateId(
                field = "currentWarehouseId",
                value = vehicle.currentWarehouse.id,
                allowedPrefixes = setOf("WH-")
            ),
            errors
        )

        if (errors.isNotEmpty()) {
            return ValidationResult.Failure(errors)
        }

        return ValidationResult.Success
    }

    private fun collectErrors(
        result: ValidationResult,
        errors: MutableList<ValidationError>
    ) {
        if (result is ValidationResult.Failure) {
            errors.addAll(result.errors)
        }
    }
}
