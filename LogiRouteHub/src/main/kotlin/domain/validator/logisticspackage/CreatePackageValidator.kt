package domain.validator.logisticspackage

import domain.model.Package
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreatePackageValidator {

    operator fun invoke(logisticsPackage: Package): ValidationResult {

        val errors = mutableListOf<ValidationError>()
        collectErrors(
            CommonValidators.validateId(
                field = "id",
                value = logisticsPackage.id,
                allowedPrefixes = setOf("PKG-")
            ),
            errors
        )
        collectErrors(
            CommonValidators.validatePositiveNumber(
                field = "weightByKg",
                value = logisticsPackage.weightByKg
            ),
            errors
        )
        collectErrors(
            validatePriority(logisticsPackage.priority),
            errors
        )
        collectErrors(
            CommonValidators.validateId(
                field = "originWarehouseId",
                value = logisticsPackage.originWarehouse.id,
                allowedPrefixes = setOf("WH-")
            ),
            errors
        )
        collectErrors(
            CommonValidators.validateId(
                field = "destinationWarehouseId",
                value = logisticsPackage.destinationWarehouse.id,
                allowedPrefixes = setOf("WH-")
            ),
            errors
        )

        if (errors.isNotEmpty()) {
            return ValidationResult.Failure(errors)
        }

        return ValidationResult.Success
    }

    private fun validatePriority(
        priority: String
    ): ValidationResult {

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
