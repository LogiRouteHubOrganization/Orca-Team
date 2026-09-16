package domain.validator.logisticspackage

import domain.model.updateinput.PackageUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class UpdatePackageValidator {

    operator fun invoke(updateModel: PackageUpdateModel): ValidationResult {

        val errors = mutableListOf<ValidationError>()

        if (
            updateModel.weightByKg == null &&
            updateModel.priority == null &&
            updateModel.originWarehouseId == null &&
            updateModel.destinationWarehouseId == null
        ) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = "update",
                        reason = "At least one field must be provided for update"
                    )
                )
            )
        }

        updateModel.weightByKg?.let { weight ->
            collectErrors(
                CommonValidators.validatePositiveNumber(
                    field = "weightByKg",
                    value = weight
                ),
                errors
            )
        }

        updateModel.priority?.let { priority ->
            collectErrors(
                validatePriority(priority),
                errors
            )
        }

        updateModel.originWarehouseId?.let { warehouseId ->
            collectErrors(
                CommonValidators.validateId(
                    field = "originWarehouseId",
                    value = warehouseId,
                    allowedPrefixes = setOf("WH-")
                ),
                errors
            )
        }

        updateModel.destinationWarehouseId?.let { warehouseId ->
            collectErrors(
                CommonValidators.validateId(
                    field = "destinationWarehouseId",
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

    private fun validatePriority(priority: String): ValidationResult {

        val allowedPriorities = setOf(
            "STANDARD",
            "LOW",
            "URGENT"
        )

        if (priority !in allowedPriorities) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = "priority",
                        reason = "Priority must be STANDARD, LOW, or URGENT"
                    )
                )
            )
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
