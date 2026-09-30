package domain.validator.rule

import domain.validator.ErrorValidator
import domain.validator.ResultValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return valid for minimum valid longitude`() {
        // Given
        val longitude = -180.0

        // When
        val result = validator.validate(longitude)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return valid for maximum valid longitude`() {
        // Given
        val longitude = 180.0

        // When
        val result = validator.validate(longitude)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return invalid when longitude is below minimum`() {
        // Given
        val longitude = -180.01

        // When
        val result = validator.validate(longitude)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val failure = result as ResultValidator.Invalid

        assertEquals(
            ErrorValidator.InvalidLongitude,
            failure.errors.first().error
        )
    }

    @Test
    fun `should return invalid when longitude is above maximum`() {
        // Given
        val longitude = 180.01

        // When
        val result = validator.validate(longitude)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val failure = result as ResultValidator.Invalid

        assertEquals(
            ErrorValidator.InvalidLongitude,
            failure.errors.first().error
        )
    }

    @Test
    fun `should return invalid when longitude is null`() {
        // Given
        val longitude: Double? = null

        // When
        val result = validator.validate(longitude)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val failure = result as ResultValidator.Invalid

        assertEquals(
            ErrorValidator.InvalidLongitude,
            failure.errors.first().error
        )
    }
}