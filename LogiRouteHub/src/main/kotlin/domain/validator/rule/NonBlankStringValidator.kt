package domain.validator.rule

import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class NonBlankStringValidator(
    private val field: String
) : Validator<String?> {

    override fun validate(value: String?): ResultValidator {

        return if (!value.isNullOrBlank()) {
            ResultValidator.Valid
        } else {
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(field = field, error = ErrorValidator.BlankValue)
                )
            )
        }
    }
}
