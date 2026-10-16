
package mainTest.domainTest.validatorTest.telemetryTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.rule.FuelPercentageValidator
import org.junit.jupiter.api.Test

class FuelPercentageValidatorTest {

    private val validator = FuelPercentageValidator()

    @Test
    fun `should return valid when fuel percentage is zero`() {
        assertThat(validator.validate(0.0))
            .isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return valid when fuel percentage is one hundred`() {
        assertThat(validator.validate(100.0))
            .isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when fuel percentage is negative`() {
        assertThat(validator.validate(-1.0))
            .isEqualTo(
                ResultValidator.Invalid(
                    listOf(
                        FieldValidator(
                            field = "fuelPercentage",
                            error = ErrorValidator.InvalidFuelPercentage
                        )
                    )
                )
            )
    }

    @Test
    fun `should return invalid when fuel percentage exceeds one hundred`() {
        assertThat(validator.validate(101.0))
            .isInstanceOf(ResultValidator.Invalid::class.java)
    }

    @Test
    fun `should return invalid when fuel percentage is null`() {
        assertThat(validator.validate(null))
            .isInstanceOf(ResultValidator.Invalid::class.java)
    }

    @Test
    fun `should return invalid when fuel percentage is NaN`() {
        assertThat(validator.validate(Double.NaN))
            .isInstanceOf(ResultValidator.Invalid::class.java)
    }

    @Test
    fun `should return invalid when fuel percentage is positive infinity`() {
        assertThat(validator.validate(Double.POSITIVE_INFINITY))
            .isInstanceOf(ResultValidator.Invalid::class.java)
    }
}

