package domain.validator.vehicle

import domain.model.updateinput.VehicleUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class UpdateVehicleValidator {

    operator fun invoke(updateModel: VehicleUpdateModel): ValidationResult {

        val errors = mutableListOf<ValidationError>()

        if (updateModel.maxCapacityKg == null &&
            updateModel.costPerKm == null &&
            updateModel.currentWarehouseId == null
        ) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = "request",
                        reason = "At least one field must be provided for update"
                    )
                )
            )
        }

        updateModel.maxCapacityKg?.let { maxCapacity ->
            collectErrors(
                CommonValidators.validatePositiveNumber(
                    field = "maxCapacityKg",
                    value = maxCapacity
                ),
                errors
            )
        }

        updateModel.costPerKm?.let { costPerKm ->
            collectErrors(
                CommonValidators.validatePositiveNumber(
                    field = "costPerKm",
                    value = costPerKm
                ),
                errors
            )
        }

        updateModel.currentWarehouseId?.let { warehouseId ->
            collectErrors(
                CommonValidators.validateId(
                    field = "currentWarehouseId",
                    value = warehouseId,
                    allowedPrefixes = setOf("WH-")
                ),
                errors
            )
        }

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
