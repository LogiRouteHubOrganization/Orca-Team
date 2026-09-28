package domain.validator.route

import domain.model.updateinput.RouteUpdateModel
import domain.validator.FieldViolation
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
        if (!hasAnyUpdate(updateModel)) {
            return ValidationResult.Invalid(
                listOf(FieldViolation(field = "update", error = ValidationError.EmptyUpdate))
            )
        }

        val whPrefix = setOf("WH-")
        val results = listOfNotNull(
            IdValidator(field = "routeId", allowedPrefixes = setOf("RT-")).validate(routeId),
            updateModel.originWarehouseId?.let {
                IdValidator(field = "originWarehouseId", allowedPrefixes = whPrefix).validate(it)
            },
            updateModel.destinationWarehouseId?.let {
                IdValidator(field = "destinationWarehouseId", allowedPrefixes = whPrefix).validate(it)
            },
            updateModel.distanceKm?.let {
                PositiveNumberValidator(field = "distanceKm").validate(it)
            },
            updateModel.typicalDelayMin?.let {
                NonNegativeNumberValidator(field = "typicalDelayMin").validate(it)
            }
        )

        val errors = results.filterIsInstance<ValidationResult.Invalid>().flatMap { it.errors }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    private fun hasAnyUpdate(updateModel: RouteUpdateModel): Boolean {
        return listOf(
            updateModel.originWarehouseId,
            updateModel.destinationWarehouseId,
            updateModel.distanceKm,
            updateModel.typicalDelayMin
        ).any { it != null }
    }
}
