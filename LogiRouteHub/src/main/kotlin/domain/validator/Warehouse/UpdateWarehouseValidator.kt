package domain.validator.Warehouse


import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class UpdateWarehouseValidator {

    operator fun invoke(
        update: WarehouseUpdateModel
    ): ValidationResult {

        val results = listOfNotNull(
            update.name?.let {
                CommonValidators.validateString(
                    field = "name",
                    value = it
                )
            },

            update.regionalZone?.let {
                CommonValidators.validateString(
                    field = "regionalZone",
                    value = it
                )
            },

            update.latitude?.let {
                CommonValidators.validatePositiveNumber(
                    field = "latitude",
                    value = it
                )
            },

            update.longitude?.let {
                CommonValidators.validatePositiveNumber(
                    field = "longitude",
                    value = it
                )
            }
        )

        val errors = results
            .filterIsInstance<ValidationResult.Failure>()
            .flatMap { it.errors }

        val hasAtLeastOneUpdate = listOf(
            update.name,
            update.regionalZone,
            update.latitude,
            update.longitude
        ).any { it != null }

        val finalErrors = errors + if (!hasAtLeastOneUpdate) {
            listOf(
                ValidationError(
                    field = "warehouse",
                    reason = "At least one field must be provided for update"
                )
            )
        } else {
            emptyList()
        }

        return if (finalErrors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(finalErrors)
        }
    }
}