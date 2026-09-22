package domain.validator.vehicle

import domain.model.updateinput.VehicleUpdateModel
import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator

class UpdateVehicleValidator {

    operator fun invoke(updateModel: VehicleUpdateModel): ValidationResult {
        if (!hasAnyUpdate(updateModel)) {
            return ValidationResult.Failure(
                listOf(FieldViolation(field = "update", error = ValidationError.EmptyUpdate))
            )
        }

        val results = listOfNotNull(
            updateModel.maxCapacityKg?.let {
                PositiveNumberValidator(field = "maxCapacityKg").validate(it)
            },
            updateModel.costPerKm?.let {
                PositiveNumberValidator(field = "costPerKm").validate(it)
            },
            updateModel.currentWarehouseId?.let {
                IdValidator(field = "currentWarehouseId", allowedPrefixes = setOf("WH-")).validate(it)
            }
        )

        val errors = results.filterIsInstance<ValidationResult.Failure>().flatMap { it.errors }
        return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Failure(errors)
    }

    private fun hasAnyUpdate(updateModel: VehicleUpdateModel): Boolean {
        return listOf(
            updateModel.maxCapacityKg,
            updateModel.costPerKm,
            updateModel.currentWarehouseId
        ).any { it != null }
    }
}
