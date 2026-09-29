package domain.validator.logisticspackage

import domain.model.Warehouse
import domain.validator.ResultVaildator
import domain.validator.rule.DifferentLocationsValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.PositiveNumberValidator
import domain.validator.rule.PriorityValidator

class CreatePackageValidator {

    operator fun invoke(
        id: String,
        weightByKg: Double,
        priority: String,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): ResultVaildator {

        val results = listOf(

            IdValidator(
                field = "id",
                allowedPrefixes = setOf("PKG-")
            ).validate(id),


            PositiveNumberValidator(
                field = "weightByKg"
            ).validate(weightByKg),


            PriorityValidator(
                field = "priority"
            ).validate(priority),


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
            )
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
