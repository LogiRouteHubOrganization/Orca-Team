package domain.validator.rule

import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class PriorityValidator(
    private val field: String,
    private val allowedPriorities: Set<String> = setOf(
        "STANDARD",
        "LOW",
        "URGENT"
    )
) : Validator<String?> {

    override fun validate(value: String?): ValidationResult {

        if (value.isNullOrBlank()) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Priority must not be blank"
                    )
                )
            )
        }

        return if (value in allowedPriorities) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Priority must be one of: ${allowedPriorities.joinToString()}"
                    )
                )
            )
        }
    }
}
