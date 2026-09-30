package domain.validator.rule

import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class PositiveNumberValidator(
    private val field: String
) : Validator<Number?> {

    override fun validate(value: Number?): ResultValidator {

        return if (value != null && value.toDouble() > 0) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(field = field, error = ErrorValidator.MustBePositive)
                )
            )
        }
    }
}
