package domain.validator.rule

import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class NonBlankStringValidator(
    private val field: String
) : Validator<String?> {

    override fun validate(value: String?): ValidationResult {

        return if (!value.isNullOrBlank()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Value must not be blank"
                    )
                )
            )
        }
    }
}
