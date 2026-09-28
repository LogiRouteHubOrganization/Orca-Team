package domain.validator.warehouse

import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import domain.validator.rule.NonBlankStringValidator

class UpdateWarehouseValidator {

    operator fun invoke(updateModel: WarehouseUpdateModel): ValidationResult {
        if (!hasAnyUpdate(updateModel)) {
            return ValidationResult.Invalid(
                listOf(FieldViolation(field = "update", error = ValidationError.EmptyUpdate))
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

        val errors = results.filterIsInstance<ValidationResult.Invalid>().flatMap { it.errors }

        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
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

