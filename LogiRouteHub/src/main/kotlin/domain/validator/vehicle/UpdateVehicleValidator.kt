package domain.validator.vehicle

import domain.model.updateinput.VehicleUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class UpdateVehicleValidator {

    operator fun invoke(
        updateModel: VehicleUpdateModel
    ): ValidationResult {

        val hasUpdate = listOf(
            updateModel.maxCapacityKg,
            updateModel.costPerKm,
            updateModel.currentWarehouseId
        ).any { it != null }

        if (!hasUpdate) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = "request",
                        reason = "At least one field must be provided for update"
                    )
                )
            )
        }

        val results = listOf(
            updateModel.maxCapacityKg?.let { maxCapacity ->
                CommonValidators.validatePositiveNumber(
                    field = "maxCapacityKg",
                    value = maxCapacity
                )
            },

            updateModel.costPerKm?.let { costPerKm ->
                CommonValidators.validatePositiveNumber(
                    field = "costPerKm",
                    value = costPerKm
                )
            },

            updateModel.currentWarehouseId?.let { warehouseId ->
                CommonValidators.validateId(
                    field = "currentWarehouseId",
                    value = warehouseId,
                    allowedPrefixes = setOf("WH-")
                )
            }
        ).filterNotNull()

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