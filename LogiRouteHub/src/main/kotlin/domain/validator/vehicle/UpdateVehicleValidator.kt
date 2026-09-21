package domain.validator.vehicle

import domain.model.updateinput.VehicleUpdateModel
import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator

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
                    FieldViolation(field = "update", error = ValidationError.EmptyUpdate)
                )
            )
        }

        val results = listOfNotNull(
            updateModel.maxCapacityKg?.let { maxCapacity ->
                PositiveNumberValidator(
                    field = "maxCapacityKg"
                ).validate(maxCapacity)
            },
            updateModel.costPerKm?.let { cost ->
                PositiveNumberValidator(
                    field = "costPerKm"
                ).validate(cost)
            },
            updateModel.currentWarehouseId?.let { warehouseId ->
                IdValidator(
                    field = "currentWarehouseId",
                    allowedPrefixes = setOf("WH-")
                ).validate(warehouseId)
            }
        )
        val errors = results.filterIsInstance<ValidationResult.Failure>()
            .flatMap { it.errors }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }
}
