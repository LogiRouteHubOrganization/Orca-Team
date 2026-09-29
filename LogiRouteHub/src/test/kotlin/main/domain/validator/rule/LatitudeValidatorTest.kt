package domain.validator.rule

import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return success for minimum valid latitude`() {
        // Given
        val latitude = -90.0

        // When
        val result = validator.validate(latitude)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return success for maximum valid latitude`() {
        // Given
        val latitude = 90.0

        // When
        val result = validator.validate(latitude)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return failure when latitude is below minimum`() {
        // Given
        val latitude = -90.01

        // When
        val result = validator.validate(latitude)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val failure = result as ResultValidator.Invalid

        assertEquals(
            ErrorValidator.InvalidLatitude,
            failure.errors.first().error
        )
    }

    @Test
    fun `should return failure when latitude is above maximum`() {
        // Given
        val latitude = 90.01

        // When
        val result = validator.validate(latitude)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val failure = result as ResultValidator.Invalid

        assertEquals(
            ErrorValidator.InvalidLatitude,
            failure.errors.first().error
        )
    }

    @Test
    fun `should return failure when latitude is null`() {
        // Given
        val latitude: Double? = null

        // When
        val result = validator.validate(latitude)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val failure = result as ResultValidator.Invalid

        assertEquals(
            ErrorValidator.InvalidLatitude,
            failure.errors.first().error
        )
    }

}