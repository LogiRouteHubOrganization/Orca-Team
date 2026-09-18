package domain.validator.rule

import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class PositiveNumberValidator(
    private val field: String
) : Validator<Number?> {

    override fun validate(value: Number?): ValidationResult {

        return if (value != null && value.toDouble() > 0) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Value must be greater than 0"
                    )
                )
            )
        }
    }
}
