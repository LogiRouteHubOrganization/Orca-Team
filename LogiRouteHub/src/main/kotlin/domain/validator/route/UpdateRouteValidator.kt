package domain.validator.route

import domain.model.updateinput.RouteUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class UpdateRouteValidator {

    operator fun invoke(
        routeId: String,
        updateModel: RouteUpdateModel
    ): ValidationResult {

        val hasUpdate = listOf(
            updateModel.originWarehouseId,
            updateModel.destinationWarehouseId,
            updateModel.distanceKm,
            updateModel.typicalDelayMin
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
            CommonValidators.validateId(
                field = "routeId",
                value = routeId,
                allowedPrefixes = setOf("RT-")
            ),

            updateModel.originWarehouseId?.let {
                CommonValidators.validateId(
                    field = "originWarehouseId",
                    value = it,
                    allowedPrefixes = setOf("WH-")
                )
            },

            updateModel.destinationWarehouseId?.let {
                CommonValidators.validateId(
                    field = "destinationWarehouseId",
                    value = it,
                    allowedPrefixes = setOf("WH-")
                )
            },

            updateModel.distanceKm?.let {
                CommonValidators.validatePositiveNumber(
                    field = "distanceKm",
                    value = it
                )
            },

            updateModel.typicalDelayMin?.let {
                validateTypicalDelay(it)
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