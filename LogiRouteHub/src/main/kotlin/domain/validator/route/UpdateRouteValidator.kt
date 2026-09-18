package domain.validator.route

import domain.model.updateinput.RouteUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.NonNegativeNumberValidator
import domain.validator.rule.PositiveNumberValidator

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

        val results = listOfNotNull(IdValidator(
                field = "routeId",
                allowedPrefixes = setOf("RT-")
            ).validate(routeId),

            updateModel.originWarehouseId?.let { originId ->
                IdValidator(
                    field = "originWarehouseId",
                    allowedPrefixes = setOf("WH-")
                ).validate(originId)
            },

            updateModel.destinationWarehouseId?.let { destinationId ->
                IdValidator(
                    field = "destinationWarehouseId",
                    allowedPrefixes = setOf("WH-")
                ).validate(destinationId)
            },

            updateModel.distanceKm?.let { distance ->
                PositiveNumberValidator(
                    field = "distanceKm"
                ).validate(distance)
            },

            updateModel.typicalDelayMin?.let { delay ->
                NonNegativeNumberValidator(
                    field = "typicalDelayMin"
                ).validate(delay)
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
