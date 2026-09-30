package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.rule.NonNegativeNumberValidator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NonNegativeNumberValidatorTest {

    private lateinit var validator: NonNegativeNumberValidator

    @BeforeEach
    fun setUp() {
        validator = NonNegativeNumberValidator(field = "capacity")
    }

    @Test
    fun `should return valid when value is positive`() {
        // Given
        val value = 100

        // When
        val result = validator.validate(value)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return valid when value is zero`() {
        // Given
        val value = 0

        // When
        val result = validator.validate(value)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when value is negative`() {
        // Given
        val value = -50

        // When
        val result = validator.validate(value)

        // Then
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "capacity",
                        error = ErrorValidator.MustBeNonNegative
                    )
                )
            )
        )
    }

    @Test
    fun `should return invalid when value is null`() {
        // Given
        val value: Number? = null

        // When
        val result = validator.validate(value)

        // Then
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "capacity",
                        error = ErrorValidator.MustBeNonNegative
                    )
                )
            )
        )
    }

    @Test
    fun `should return valid when value is positive decimal`() {
        // Given
        val value = 10.5

        // When
        val result = validator.validate(value)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when value is negative decimal`() {
        // Given
        val value = -5.5

        // When
        val result = validator.validate(value)

        // Then
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "capacity",
                        error = ErrorValidator.MustBeNonNegative
                    )
                )
            )
        )
    }
}
