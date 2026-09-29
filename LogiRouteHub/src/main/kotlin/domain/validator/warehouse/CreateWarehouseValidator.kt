package domain.validator.warehouse

import domain.validator.ResultValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import domain.validator.rule.NonBlankStringValidator

class CreateWarehouseValidator {

    operator fun invoke(
        id: String,
        name: String,
        regionalZone: String,
        latitude: Double,
        longitude: Double
    ): ResultValidator {


        val results: List<ResultValidator> = listOf(
            IdValidator(
                field = "id",
                allowedPrefixes = setOf("WH-")
            ).validate(id),

            NonBlankStringValidator(
                field = "name"
            ).validate(name),

            NonBlankStringValidator(
                field = "regionalZone"
            ).validate(regionalZone),

            LatitudeValidator(
                field = "latitude"
            ).validate(latitude),

            LongitudeValidator(
                field = "longitude"
            ).validate(longitude)
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
