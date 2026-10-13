package domain.validator.vehicle

import domain.model.Warehouse
import domain.validator.ResultValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator

class CreateVehicleValidator {

    operator fun invoke(
        id: String,
        maxCapacityKg: Double,
        costPerKm: Double,
        currentWarehouse: Warehouse,
        maxVolumeM3: Double? = null
    ): ResultValidator {

        val results: List<ResultValidator> = listOfNotNull(

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

            maxVolumeM3?.let {
                PositiveNumberValidator(
                    field = "maxVolumeM3"
                ).validate(it)
            },

            IdValidator(
                field = "currentWarehouseId",
                allowedPrefixes = setOf("WH-")
            ).validate(currentWarehouse.id)
        )

        val errors = results
            .filterIsInstance<ResultValidator.Invalid>()
            .flatMap { it.errors }

        return if (errors.isEmpty()) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(errors)
        }
    }
}
