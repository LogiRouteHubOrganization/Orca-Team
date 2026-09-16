package domain.validator.logisticspackage

import domain.model.Package
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreatePackageValidator {

    operator fun invoke(logisticsPackage: Package): ValidationResult {

        val results = listOf(
            CommonValidators.validateId(
                field = "id",
                value = logisticsPackage.id,
                allowedPrefixes = setOf("PKG-")
            ),

            CommonValidators.validatePositiveNumber(
                field = "weightByKg",
                value = logisticsPackage.weightByKg
            ),

            validatePriority(logisticsPackage.priority),

            CommonValidators.validateId(
                field = "originWarehouseId",
                value = logisticsPackage.originWarehouse.id,
                allowedPrefixes = setOf("WH-")
            ),

            CommonValidators.validateId(
                field = "destinationWarehouseId",
                value = logisticsPackage.destinationWarehouse.id,
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