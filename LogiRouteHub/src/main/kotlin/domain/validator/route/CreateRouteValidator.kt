package domain.validator.route

import domain.model.Route
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreateRouteValidator {

    operator fun invoke(route: Route): ValidationResult {

        val results = listOf(
            CommonValidators.validateId(
                field = "id",
                value = route.id,
                allowedPrefixes = setOf("RT-")
            ),

            CommonValidators.validateId(
                field = "originWarehouseId",
                value = route.originWarehouse.id,
                allowedPrefixes = setOf("WH-")
            ),

            CommonValidators.validateId(
                field = "destinationWarehouseId",
                value = route.destinationWarehouse.id,
                allowedPrefixes = setOf("WH-")
            ),

            CommonValidators.validatePositiveNumber(
                field = "distanceKm",
                value = route.distanceKm
            ),

            validateTypicalDelay(route.typicalDelayMin)
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

    private fun validateTypicalDelay(
        typicalDelayMin: Int
    ): ValidationResult {
        return if (typicalDelayMin < 0) {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = "typicalDelayMin",
                        reason = "Typical delay must be greater than or equal to 0"
                    )
                )
            )
        } else {
            ValidationResult.Success
        }
    }
}