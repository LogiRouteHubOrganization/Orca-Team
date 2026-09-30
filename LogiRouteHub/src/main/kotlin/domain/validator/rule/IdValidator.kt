package domain.validator.rule

import domain.validator.FieldValidator
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.Validator

class IdValidator(
    private val field: String,
    private val allowedPrefixes: Set<String>
) : Validator<String?> {

    override fun validate(value: String?): ResultValidator {
        if (value.isNullOrBlank()) {
            return ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = field,
                        error = ErrorValidator.BlankId
                    )
                )
            )
        }
        val matchedPrefix = allowedPrefixes.firstOrNull { prefix ->
            value.startsWith(prefix)
        }
        if (matchedPrefix == null) {
            return ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = field,
                        error = ErrorValidator.InvalidIdPrefix(
                            allowedPrefixes = allowedPrefixes
                        )
                    )
                )
            )
        }
        val numericPart = value.removePrefix(matchedPrefix)

        if (!numericPart.matches(Regex("\\d{6}"))) {
            return ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = field,
                        error = ErrorValidator.InvalidIdFormat
                    )
                )
            )
        }
        return ResultValidator.Valid
    }
}
