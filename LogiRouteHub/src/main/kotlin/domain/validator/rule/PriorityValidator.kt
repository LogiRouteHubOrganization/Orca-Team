package domain.validator.rule

import domain.validator.FieldViolation
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
                    FieldViolation(field = field, error = ValidationError.BlankPriority)
                )
            )
        }
        return if (value in allowedPriorities) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        field = field,
                        error = ValidationError.InvalidPriority(
                            allowedPriorities = allowedPriorities
                        )
                    )
                )
            )
        }
    }
}
