package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.rule.NonBlankStringValidator
import org.junit.jupiter.api.Test

class NonBlankStringValidatorTest {
    @Test
    fun `should return valid when value is a non blank string`() {
        // Given
        val validator = NonBlankStringValidator(field = "name")

        // When
        val result = validator.validate("Main Warehouse")

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when value is null`() {
        // Given
        val validator = NonBlankStringValidator(field = "name")

        // When
        val result = validator.validate(null)

        // Then
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(FieldValidator(
                        field = "name",
                        error = ErrorValidator.BlankValue
                    )
                )
            )
        )
    }

    @Test
    fun `should return invalid when value is empty`() {
        // Given
        val validator = NonBlankStringValidator(field = "name")

        // When
        val result = validator.validate("")

        // Then
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(FieldValidator(
                        field = "name",
                        error = ErrorValidator.BlankValue
                    )
                )
            )
        )
    }

    @Test
    fun `should return invalid when value contains only spaces`() {
        // Given
        val validator = NonBlankStringValidator(field = "name")

        // When
        val result = validator.validate("   ")

        // Then
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(FieldValidator(
                        field = "name",
                        error = ErrorValidator.BlankValue
                    )
                )
            )
        )
    }
}
