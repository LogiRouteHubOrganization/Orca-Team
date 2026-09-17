package domain.validator.warehouse

import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

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
                        field = "warehouse",
                        reason = "At least one field must be provided for update"
                    )
                )
            )
        }

        val results = listOf(
            updateModel.name?.let {
                CommonValidators.validateString(
                    field = "name",
                    value = it
                )
            },

            updateModel.regionalZone?.let {
                CommonValidators.validateString(
                    field = "regionalZone",
                    value = it
                )
            },

            updateModel.latitude?.let {
                CommonValidators.validatePositiveNumber(
                    field = "latitude",
                    value = it
                )
            },

            updateModel.longitude?.let {
                CommonValidators.validatePositiveNumber(
                    field = "longitude",
                    value = it
                )
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
}
