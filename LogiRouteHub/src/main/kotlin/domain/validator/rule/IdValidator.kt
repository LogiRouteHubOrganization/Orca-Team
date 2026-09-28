package domain.validator.rule

import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class IdValidator(
    private val field: String,
    private val allowedPrefixes: Set<String>
) : Validator<String?> {

    override fun validate(value: String?): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.Invalid(
                listOf(
                    FieldViolation(
                        field = field,
                        error = ValidationError.BlankId
                    )
                )
            )
        }
        val matchedPrefix = allowedPrefixes.firstOrNull { prefix ->
            value.startsWith(prefix)
        }
        if (matchedPrefix == null) {
            return ValidationResult.Invalid(
                listOf(
                    FieldViolation(
                        field = field,
                        error = ValidationError.InvalidIdPrefix(
                            allowedPrefixes = allowedPrefixes
                        )
                    )
                )
            )
        }
        val numericPart = value.removePrefix(matchedPrefix)

        if (!numericPart.matches(Regex("\\d{6}"))) {
            return ValidationResult.Invalid(
                listOf(
                    FieldViolation(
                        field = field,
                        error = ValidationError.InvalidIdFormat
                    )
                )
            )
        }
        return ValidationResult.Valid
    }
}
