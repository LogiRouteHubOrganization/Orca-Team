package domain.validator

sealed class ValidationResult {

    data object Valid : ValidationResult()

    data class Invalid(
        val errors: List<FieldViolation>
    ) : ValidationResult()
}
