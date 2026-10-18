package domain.model.request

import domain.exception.EntityValidationException
import domain.validator.ResultValidator
import domain.validator.rule.FuelPercentageValidator
import domain.validator.rule.IdValidator
import domain.validator.rule.LatitudeValidator
import domain.validator.rule.LongitudeValidator
import domain.validator.rule.NonNegativeNumberValidator
import java.time.Instant

data class VehicleTelemetry(
    val vehicleId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val fuelPercentage: Double,
    val timestamp: Instant
) {
    init {
        val results = listOf(
            IdValidator(
                field = "vehicleId",
                allowedPrefixes = setOf("TRK-")
            ).validate(vehicleId),

            LatitudeValidator(field = "latitude").validate(latitude),

            LongitudeValidator(field = "longitude").validate(longitude),

            NonNegativeNumberValidator(field = "speedKmh")
                .validate(speedKmh),

            FuelPercentageValidator().validate(fuelPercentage)
        )

        val errors = results
            .filterIsInstance<ResultValidator.Invalid>()
            .flatMap { it.errors }

        if (errors.isNotEmpty()) {
            throw EntityValidationException(errors)
        }
    }
}
