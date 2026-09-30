package domain.validator.warehouse

import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import domain.validator.rule.NonBlankStringValidator

class UpdateWarehouseValidator {

    operator fun invoke(updateModel: WarehouseUpdateModel): ResultValidator {
        if (!hasAnyUpdate(updateModel)) {
            return ResultValidator.Invalid(
                listOf(FieldValidator(field = "update", error = ErrorValidator.EmptyUpdate))
            )
        }

        val results = listOfNotNull(
            updateModel.name?.let {
                NonBlankStringValidator("name").validate(it) },
            updateModel.regionalZone?.let {
                NonBlankStringValidator("regionalZone").validate(it) },
            updateModel.latitude?.let {
                LatitudeValidator("latitude").validate(it) },
            updateModel.longitude?.let {
                LongitudeValidator("longitude").validate(it) }
        )

        val errors = results.filterIsInstance<ResultValidator.Invalid>().flatMap { it.errors }

        return if (errors.isEmpty()) ResultValidator.Valid else ResultValidator.Invalid(errors)
    }

    private fun hasAnyUpdate(updateModel: WarehouseUpdateModel): Boolean {
        return listOf(
            updateModel.name,
            updateModel.regionalZone,
            updateModel.latitude,
            updateModel.longitude
        ).any { it != null }
    }
}

