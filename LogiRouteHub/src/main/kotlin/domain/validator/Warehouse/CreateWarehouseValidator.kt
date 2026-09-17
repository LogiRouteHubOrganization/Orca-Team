package domain.validator.Warehouse

import domain.model.Warehouse
import domain.validator.ValidationResult
import domain.validator.common.CommonValidators

class CreateWarehouseValidator {

    operator fun invoke(warehouse: Warehouse): ValidationResult {

        val results = listOf(
            CommonValidators.validateId(
                field = "id",
                value = warehouse.id,
                allowedPrefixes = setOf("WH-")
            ),
            CommonValidators.validateString(
                field = "name",
                value = warehouse.name
            ),
            CommonValidators.validateString(
                field = "regionalZone",
                value = warehouse.regionalZone
            ),
            CommonValidators.validatePositiveNumber(
                field = "latitude",
                value = warehouse.latitude
            ),
            CommonValidators.validatePositiveNumber(
                field = "longitude",
                value = warehouse.longitude
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
