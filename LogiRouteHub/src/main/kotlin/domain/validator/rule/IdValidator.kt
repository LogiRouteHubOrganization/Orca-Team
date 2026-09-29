package domain.validator.rule

import domain.validator.FieldViolator
import domain.validator.ErrorValidator
import domain.validator.ResultVaildator
import domain.validator.Validator

class IdValidator(
    private val field: String,
    private val allowedPrefixes: Set<String>
) : Validator<String?> {

    override fun validate(value: String?): ResultVaildator {
        if (value.isNullOrBlank()) {
            return ResultVaildator.Invalid(
                listOf(
                    FieldViolator(
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
            return ResultVaildator.Invalid(
                listOf(
                    FieldViolator(
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
            return ResultVaildator.Invalid(
                listOf(
                    FieldViolator(
                        field = field,
                        error = ErrorValidator.InvalidIdFormat
                    )
                )
            )
        }
        return ResultVaildator.Valid
    }
}
