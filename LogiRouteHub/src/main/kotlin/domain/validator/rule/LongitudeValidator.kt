package domain.validator.rule

import domain.validator.ValidationError
import domain.validator.ValidationResult
import domain.validator.Validator

class LongitudeValidator(
    private val field: String = "longitude"
) : Validator<Number?> {

    override fun validate(value: Number?): ValidationResult {

        return if (value != null && value.toDouble() in -180.0..180.0) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(
                listOf(
                    ValidationError(
                        field = field,
                        reason = "Longitude must be between -180 and 180"
                    )
                )
            )
        }
    }
}
