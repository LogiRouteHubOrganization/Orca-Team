package domain.validator.rule

import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class FuelPercentageValidator : Validator<Number?> {

    override fun validate(value: Number?): ResultValidator {
        val percentage = value?.toDouble()

        return if (
            percentage != null &&
            percentage.isFinite() &&
            percentage in 0.0..100.0
        ) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "fuelPercentage",
                        error = ErrorValidator.InvalidFuelPercentage
                    )
                )
            )
        }
    }
}