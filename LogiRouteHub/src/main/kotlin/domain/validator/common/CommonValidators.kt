package domain.validator.common

import domain.validator.ValidationError
import domain.validator.ValidationResult
import java.util.UUID

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
                        field,
                        "ID must not be blank"
                    )
                )
            )
        }

        val hasValidPrefix = allowedPrefixes.any { prefix ->
            value.startsWith(prefix)
        }

        val isValidUuid = try {
            UUID.fromString(value)
            true
        } catch (_: IllegalArgumentException) {
            false
        }

        if (!hasValidPrefix && !isValidUuid) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field,
                        "ID must have a valid prefix or UUID format"
                    )
                )
            )
        }

        return ValidationResult.Success
    }

    fun validateString(
        field: String,
        value: String?
    ): ValidationResult {
        if (value.isNullOrBlank()) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field,
                        "Value must not be blank"
                    )
                )
            )
        }

        return ValidationResult.Success
    }

    fun validatePositiveNumber(
        field: String,
        value: Number?
    ): ValidationResult {
        if (value == null || value.toDouble() <= 0) {
            return ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field,
                        "Value must be greater than 0"
                    )
                )
            )
        }

        return ValidationResult.Success
    }
}