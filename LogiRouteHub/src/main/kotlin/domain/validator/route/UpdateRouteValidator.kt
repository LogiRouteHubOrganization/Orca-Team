package domain.validator.route

import domain.model.updateinput.RouteUpdateModel
import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.rule.IdValidator
import domain.validator.rule.NonNegativeNumberValidator
import domain.validator.rule.PositiveNumberValidator

class UpdateRouteValidator {

    operator fun invoke(
        routeId: String,
        updateModel: RouteUpdateModel
    ): ResultVaildator {
        if (!hasAnyUpdate(updateModel)) {
            return ResultVaildator.Invalid(
                listOf(FieldViolator(field = "update", error = ErrorValidator.EmptyUpdate))
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

        val errors = results.filterIsInstance<ResultVaildator.Invalid>().flatMap { it.errors }
        return if (errors.isEmpty()) ResultVaildator.Valid else ResultVaildator.Invalid(errors)
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
