
package mainTest.domainTest.validatorTest.ruleTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import domain.validator.rule.LatitudeValidator
import org.junit.jupiter.api.Test

class LatitudeValidatorTest {

    private val validator = LatitudeValidator()

    @Test
    fun `should return success for valid latitude`() {
        // Given
        val latitude = 31.95

        // When
        val result = validator.validate(latitude)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return success for minimum valid latitude`() {
        // Given
        val latitude = -90.0

        // When
        val result = validator.validate(latitude)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return success for maximum valid latitude`() {
        // Given
        val latitude = 90.0

        // When
        val result = validator.validate(latitude)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return failure when latitude is below minimum`() {
        // Given
        val latitude = -90.01

        // When
        val result = validator.validate(latitude)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val failure = result as ResultValidator.Invalid

        assertThat(failure.errors.first().error)
            .isEqualTo(ErrorValidator.InvalidLatitude)
    }

    @Test
    fun `should return failure when latitude is above maximum`() {
        // Given
        val latitude = 90.01

        // When
        val result = validator.validate(latitude)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val failure = result as ResultValidator.Invalid

        assertThat(failure.errors.first().error)
            .isEqualTo(ErrorValidator.InvalidLatitude)
    }

    @Test
    fun `should return failure when latitude is null`() {
        // Given
        val latitude: Double? = null

        // When
        val result = validator.validate(latitude)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val failure = result as ResultValidator.Invalid

        assertThat(failure.errors.first().error)
            .isEqualTo(ErrorValidator.InvalidLatitude)
    }
}

