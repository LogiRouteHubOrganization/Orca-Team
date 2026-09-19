package domain.model

import domain.exception.EntityValidationException
import domain.validator.ValidationResult
import domain.validator.rule.DifferentLocationsValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.NonNegativeNumberValidator
import domain.validator.rule.PositiveNumberValidator

data class Route(
    val id: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val distanceKm: Double,
    val typicalDelayMin: Int
) {

    init {
        val validationResults = listOf(
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
                Pair(
                    originWarehouse.id,
                    destinationWarehouse.id
                )
            ),

            PositiveNumberValidator(
                field = "distanceKm"
            ).validate(distanceKm),

            NonNegativeNumberValidator(
                field = "typicalDelayMin"
            ).validate(typicalDelayMin)
        )

        val errors = validationResults
            .filterIsInstance<ValidationResult.Failure>()
            .flatMap { it.errors }

        if (errors.isNotEmpty()) {
            throw EntityValidationException(errors)
        }
    }
}