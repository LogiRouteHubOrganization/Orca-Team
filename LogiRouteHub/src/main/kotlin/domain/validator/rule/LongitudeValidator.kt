package domain.validator.rule

import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.Validator

class LongitudeValidator(
    private val field: String = "longitude"
) : Validator<Number?> {

    override fun validate(value: Number?): ResultVaildator {

        return if (value != null && value.toDouble() in -180.0..180.0) {
            ResultVaildator.Valid
        } else {
            ResultVaildator.Invalid(
                listOf(
                    FieldViolator(field = field, error = ErrorValidator.InvalidLongitude)
                )
            )
        }
    }
}
