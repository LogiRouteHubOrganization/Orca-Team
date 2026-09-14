package domain.validator

data class FieldViolation(
    val field: String,
    val message: String
)

sealed class ValidationResult {

    data object Valid : ValidationResult()

    data class Invalid(
        val violations: List<FieldViolation>
    ) : ValidationResult()
}