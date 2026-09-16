package domain.validator.logisticspackage

import domain.model.updateinput.PackageUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

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
                    ValidationError(
                        field = "update",
                        reason = "At least one field must be provided for update"
                    )
                )
            )
        }

        val results = listOf(
            updateModel.weightByKg?.let { weight ->
                CommonValidators.validatePositiveNumber(
                    field = "weightByKg",
                    value = weight
                )
            },

            updateModel.priority?.let { priority ->
                validatePriority(priority)
            },

            updateModel.originWarehouseId?.let { warehouseId ->
                CommonValidators.validateId(
                    field = "originWarehouseId",
                    value = warehouseId,
                    allowedPrefixes = setOf("WH-")
                )
            },

            updateModel.destinationWarehouseId?.let { warehouseId ->
                CommonValidators.validateId(
                    field = "destinationWarehouseId",
                    value = warehouseId,
                    allowedPrefixes = setOf("WH-")
                )
            }
        )
            .filterNotNull()

        val errors = results
            .filterIsInstance<ValidationResult.Failure>()
            .flatMap { it.errors }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }

    private fun validatePriority(
        priority: String
    ): ValidationResult {

        val allowedPriorities = setOf(
            "STANDARD",
            "LOW",
            "URGENT"
        )

        return if (priority in allowedPriorities) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = "priority",
                        reason = "Priority must be STANDARD, LOW, or URGENT"
                    )
                )
            )
        }
    }
}