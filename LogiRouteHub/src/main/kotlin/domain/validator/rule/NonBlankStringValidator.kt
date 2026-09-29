package domain.validator.rule

import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.Validator

class NonBlankStringValidator(
    private val field: String
) : Validator<String?> {

    override fun validate(value: String?): ResultVaildator {

        return if (!value.isNullOrBlank()) {
            ResultVaildator.Valid
        } else {
            ResultVaildator.Invalid(
                listOf(
                    FieldViolator(field = field, error = ErrorValidator.BlankValue)
                )
            )
        }
    }
}
