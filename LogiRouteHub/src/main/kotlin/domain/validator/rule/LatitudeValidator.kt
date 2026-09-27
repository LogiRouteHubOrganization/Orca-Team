package domain.validator.rule

import domain.validator.FieldViolation
import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class LatitudeValidator(
    private val field: String = "latitude"
) : Validator<Number?> {

    override fun validate(value: Number?): ValidationResult {

        return if (value != null && value.toDouble() in -90.0..90.0) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    FieldViolation(field = field, error = ValidationError.InvalidLatitude)
                )
            )
        }
    }
}
