package domain.validator.vehicle

import domain.model.Warehouse
import domain.validator.ValidationResult
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator

class CreateVehicleValidator {

    operator fun invoke(
        id: String,
        maxCapacityKg: Double,
        costPerKm: Double,
        currentWarehouse: Warehouse
    ): ValidationResult {

        val results = listOf(

            IdValidator(
                field = "id",
                allowedPrefixes = setOf("TRK-")
            ).validate(id),


            PositiveNumberValidator(
                field = "maxCapacityKg"
            ).validate(maxCapacityKg),


            PositiveNumberValidator(
                field = "costPerKm"
            ).validate(costPerKm),


            IdValidator(
                field = "currentWarehouseId",
                allowedPrefixes = setOf("WH-")
            ).validate(currentWarehouse.id)
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
