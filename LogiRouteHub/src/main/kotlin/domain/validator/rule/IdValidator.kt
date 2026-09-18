package domain.validator.rule

import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class IdValidator(
    private val field: String,
    private val allowedPrefixes: Set<String>
) : Validator<String?> {

    override fun validate(value: String?): ValidationResult {

        if (value.isNullOrBlank()) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "ID must not be blank"
                    )
                )
            )
        }

        val matchedPrefix = allowedPrefixes.firstOrNull { prefix ->
            value.startsWith(prefix)
        }

        if (matchedPrefix == null) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "ID must start with one of: ${allowedPrefixes.joinToString()}"
                    )
                )
            )
        }

        val numericPart = value.removePrefix(matchedPrefix)

        if (!numericPart.matches(Regex("\\d{6}"))) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "ID must contain exactly 6 digits after the prefix"
                    )
                )
            )
        }

        return ValidationResult.Success
    }
}
