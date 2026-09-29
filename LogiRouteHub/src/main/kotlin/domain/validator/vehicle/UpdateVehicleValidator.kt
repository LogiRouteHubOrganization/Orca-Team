package domain.validator.vehicle

import domain.model.updateinput.VehicleUpdateModel
import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator

class UpdateVehicleValidator {

    operator fun invoke(updateModel: VehicleUpdateModel): ResultVaildator {
        if (!hasAnyUpdate(updateModel)) {
            return ResultVaildator.Invalid(
                listOf(FieldViolator(field = "update", error = ErrorValidator.EmptyUpdate))
            )
        }

        val results = listOfNotNull(
            updateModel.maxCapacityKg?.let {
                PositiveNumberValidator(field = "maxCapacityKg").validate(it)
            },
            updateModel.costPerKm?.let {
                PositiveNumberValidator(field = "costPerKm").validate(it)
            },
            updateModel.currentWarehouseId?.let {
                IdValidator(field = "currentWarehouseId", allowedPrefixes = setOf("WH-")).validate(it)
            }
        )

        val errors = results.filterIsInstance<ResultVaildator.Invalid>().flatMap { it.errors }
        return if (errors.isEmpty()) ResultVaildator.Valid else ResultVaildator.Invalid(errors)
    }

    private fun hasAnyUpdate(updateModel: VehicleUpdateModel): Boolean {
        return listOf(
            updateModel.maxCapacityKg,
            updateModel.costPerKm,
            updateModel.currentWarehouseId
        ).any { it != null }
    }
}
