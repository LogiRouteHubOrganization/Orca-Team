package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.rule.DifferentLocationsValidator
import org.junit.jupiter.api.Test

class DifferentLocationsValidatorTest {

    @Test
    fun `returns valid when locations are different`() {
        // Given
        val validator = DifferentLocationsValidator<String>()

        val locations = Pair(
            "WH-000001",
            "WH-000002"
        )

        // When
        val result = validator.validate(locations)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns different locations error when locations are the same`() {
        // Given
        val validator = DifferentLocationsValidator<String>()

        val locations = Pair(
            "WH-000001",
            "WH-000001"
        )

        // When
        val result = validator.validate(locations)

        // Then
        val expected = ResultValidator.Invalid(
            listOf(
                FieldValidator(
                    field = "values",
                    error = ErrorValidator.DifferentLocations
                )
            )
        )

        assertThat(result).isEqualTo(expected)
    }
}