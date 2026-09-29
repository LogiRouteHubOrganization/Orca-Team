package domain.validator.rule

import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class LongitudeValidator(
    private val field: String = "longitude"
) : Validator<Number?> {

    override fun validate(value: Number?): ValidationResult {

        return if (value != null && value.toDouble() in -180.0..180.0) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                listOf(
                    FieldViolation(field = field, error = ValidationError.InvalidLongitude)
                )
            )
        }
    }
}
