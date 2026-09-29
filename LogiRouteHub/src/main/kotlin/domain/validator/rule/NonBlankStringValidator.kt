package domain.validator.rule

import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class NonBlankStringValidator(
    private val field: String
) : Validator<String?> {

    override fun validate(value: String?): ValidationResult {

        return if (!value.isNullOrBlank()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                listOf(
                    FieldViolation(field = field, error = ValidationError.BlankValue)
                )
            )
        }
    }
}
