package domain.validator.common

import domain.validator.ValidationError
import domain.validator.ValidationResult

object CommonValidators {

    fun validateId(
        field: String,
        value: String?,
        allowedPrefixes: Set<String>
    ): ValidationResult {
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

        val hasValidPrefix = allowedPrefixes.any { prefix ->
            value.startsWith(prefix)
        }

        return if (hasValidPrefix) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "ID must have a valid prefix"
                    )
                )
            )
        }
    }

    fun validateString(
        field: String,
        value: String?
    ): ValidationResult {
        return if (value.isNullOrBlank()) {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Value must not be blank"
                    )
                )
            )
        } else {
            ValidationResult.Success
        }
    }

    fun validatePositiveNumber(
        field: String,
        value: Number?
    ): ValidationResult {
        return if (value == null || value.toDouble() <= 0) {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Value must be greater than 0"
                    )
                )
            )
        } else {
            ValidationResult.Success
        }
    }
}