package domain.validator.vehicle

import domain.model.Warehouse
import domain.validator.ResultVaildator
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator

class CreateVehicleValidator {

    operator fun invoke(
        id: String,
        maxCapacityKg: Double,
        costPerKm: Double,
        currentWarehouse: Warehouse
    ): ResultVaildator {

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
            .filterIsInstance<ResultVaildator.Invalid>()
            .flatMap { it.errors }

        return if (errors.isEmpty()) {
            ResultVaildator.Valid
        } else {
            ResultVaildator.Invalid(errors)
        }
    }
}
