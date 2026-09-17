package domain.validator.Warehouse

import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreateWarehouseValidator {

    operator fun invoke(
        id: String?,
        name: String?,
        regionalZone: String?,
        latitude: Double?,
        longitude: Double?
    ): ValidationResult {

        val results = listOf(
            CommonValidators.validateId(
                field = "id",
                value = id,
                allowedPrefixes = setOf("WH-")
            ),

            CommonValidators.validateString(
                field = "name",
                value = name
            ),

            CommonValidators.validateString(
                field = "regionalZone",
                value = regionalZone
            ),

            CommonValidators.validatePositiveNumber(
                field = "latitude",
                value = latitude
            ),

            CommonValidators.validatePositiveNumber(
                field = "longitude",
                value = longitude
            )
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