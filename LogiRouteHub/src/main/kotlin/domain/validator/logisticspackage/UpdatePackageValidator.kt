package domain.validator.logisticspackage

import domain.model.updateinput.PackageUpdateModel
import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator
import domain.validator.rule.PriorityValidator

class UpdatePackageValidator {

    operator fun invoke(
        updateModel: PackageUpdateModel
    ): ValidationResult {

        val hasUpdate = listOf(
            updateModel.weightByKg,
            updateModel.priority,
            updateModel.originWarehouseId,
            updateModel.destinationWarehouseId
        ).any { it != null }

        if (!hasUpdate) {
            return ValidationResult.Failure(
                listOf(
                    FieldViolation(field = "update", error = ValidationError.EmptyUpdate)
                )
            )
        }
        val results = listOfNotNull(
            updateModel.weightByKg?.let { weight ->
                PositiveNumberValidator(
                    field = "weightByKg"
                ).validate(weight)
            },

            updateModel.priority?.let { priority ->
                PriorityValidator(
                    field = "priority"
                ).validate(priority)
            },

            updateModel.originWarehouseId?.let { warehouseId ->
                IdValidator(
                    field = "originWarehouseId",
                    allowedPrefixes = setOf("WH-")
                ).validate(warehouseId)
            },

            updateModel.destinationWarehouseId?.let { warehouseId ->
                IdValidator(
                    field = "destinationWarehouseId",
                    allowedPrefixes = setOf("WH-")
                ).validate(warehouseId)
            }
        )

        val errors = results.filterIsInstance<ValidationResult.Failure>().flatMap { it.errors }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }
}
