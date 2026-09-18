package domain.validator.warehouse

import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import domain.validator.rule.NonBlankStringValidator

class UpdateWarehouseValidator {

    operator fun invoke(
        updateModel: WarehouseUpdateModel
    ): ValidationResult {

        val hasUpdate = listOf(
            updateModel.name,
            updateModel.regionalZone,
            updateModel.latitude,
            updateModel.longitude
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

        val results = listOfNotNull(
            updateModel.name?.let { name ->
                NonBlankStringValidator(
                    field = "name"
                ).validate(name)
            },

            updateModel.regionalZone?.let { zone ->
                NonBlankStringValidator(
                    field = "regionalZone"
                ).validate(zone)
            },

            updateModel.latitude?.let { lat ->
                LatitudeValidator(
                    field = "latitude"
                ).validate(lat)
            },

            updateModel.longitude?.let { lng ->
                LongitudeValidator(
                    field = "longitude"
                ).validate(lng)
            }
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
}
