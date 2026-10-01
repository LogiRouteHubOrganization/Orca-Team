
package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.rule.LongitudeValidator
import org.junit.jupiter.api.Test

class LongitudeValidatorTest {

    private val validator = LongitudeValidator()

    @Test
    fun `should return valid for valid longitude`() {
        // Given
        val longitude = 35.91

        // When
        val result = validator.validate(longitude)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return valid for minimum valid longitude`() {
        // Given
        val longitude = -180.0

        // When
        val result = validator.validate(longitude)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return valid for maximum valid longitude`() {
        // Given
        val longitude = 180.0

        // When
        val result = validator.validate(longitude)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when longitude is below minimum`() {
        // Given
        val longitude = -180.01

        // When
        val result = validator.validate(longitude)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val failure = result as ResultValidator.Invalid

        assertThat(failure.errors.first().error)
            .isEqualTo(ErrorValidator.InvalidLongitude)
    }

    @Test
    fun `should return invalid when longitude is above maximum`() {
        // Given
        val longitude = 180.01

        // When
        val result = validator.validate(longitude)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val failure = result as ResultValidator.Invalid

        assertThat(failure.errors.first().error)
            .isEqualTo(ErrorValidator.InvalidLongitude)
    }

    @Test
    fun `should return invalid when longitude is null`() {
        // Given
        val longitude: Double? = null

        // When
        val result = validator.validate(longitude)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val failure = result as ResultValidator.Invalid

        assertThat(failure.errors.first().error)
            .isEqualTo(ErrorValidator.InvalidLongitude)
    }
}

