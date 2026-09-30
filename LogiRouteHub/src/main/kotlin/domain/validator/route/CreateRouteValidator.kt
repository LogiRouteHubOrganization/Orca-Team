package domain.validator.route

import domain.model.Warehouse
import domain.validator.ResultValidator
import domain.validator.rule.DifferentLocationsValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.NonNegativeNumberValidator
import domain.validator.rule.PositiveNumberValidator

class CreateRouteValidator {

    operator fun invoke(
        id: String,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        distanceKm: Double,
        typicalDelayMin: Int
    ): ResultValidator {

        val results: List<ResultValidator> = listOf(

            IdValidator(
                field = "id",
                allowedPrefixes = setOf("RT-")
            ).validate(id),


            IdValidator(
                field = "originWarehouseId",
                allowedPrefixes = setOf("WH-")
            ).validate(originWarehouse.id),


            IdValidator(
                field = "destinationWarehouseId",
                allowedPrefixes = setOf("WH-")
            ).validate(destinationWarehouse.id),


            DifferentLocationsValidator<String>().validate(
                Pair(originWarehouse.id, destinationWarehouse.id)
            ),


            PositiveNumberValidator(
                field = "distanceKm"
            ).validate(distanceKm),


            NonNegativeNumberValidator(
                field = "typicalDelayMin"
            ).validate(typicalDelayMin)
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
