package domain.validator.rule

import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class LongitudeValidator(
    private val field: String = "longitude"
) : Validator<Number?> {

    override fun validate(value: Number?): ResultValidator {

        return if (value != null && value.toDouble() in -180.0..180.0) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(field = field, error = ErrorValidator.InvalidLongitude)
                )
            )
        }
    }
}
