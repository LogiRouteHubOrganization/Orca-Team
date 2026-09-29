package main.domain.validator.warehouse

import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.warehouse.CreateWarehouseValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WarehouseCreateValidatorTest {

    private val validator = CreateWarehouseValidator()

    @Test
    fun `should return valid when all warehouse fields are valid`() {
        // Given
        val id = "WH-123456"
        val name = "Main Warehouse"
        val regionalZone = "North"
        val latitude = 31.95
        val longitude = 35.93

        // When
        val result = validator(
            id,
            name,
            regionalZone,
            latitude,
            longitude
        )

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return invalid when warehouse id is blank`() {
        // Given
        val id = ""

        // When
        val result = validator(
            id = id,
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 31.95,
            longitude = 35.93
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "id",
                        error = ErrorValidator.BlankId
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return invalid when warehouse id has invalid prefix`() {
        // Given
        val id = "PKG-123456"

        // When
        val result = validator(
            id = id,
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 31.95,
            longitude = 35.93
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "id",
                        error = ErrorValidator.InvalidIdPrefix(
                            allowedPrefixes = setOf("WH-")
                        )
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return invalid when warehouse id has invalid format`() {
        // Given
        val id = "WH-123"

        // When
        val result = validator(
            id = id,
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 31.95,
            longitude = 35.93
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "id",
                        error = ErrorValidator.InvalidIdFormat
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return invalid when warehouse name is blank`() {
        // Given
        val name = "   "

        // When
        val result = validator(
            id = "WH-123456",
            name = name,
            regionalZone = "North",
            latitude = 31.95,
            longitude = 35.93
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "name",
                        error = ErrorValidator.BlankValue
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return invalid when regional zone is blank`() {
        // Given
        val regionalZone = ""

        // When
        val result = validator(
            id = "WH-123456",
            name = "Main Warehouse",
            regionalZone = regionalZone,
            latitude = 31.95,
            longitude = 35.93
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "regionalZone",
                        error = ErrorValidator.BlankValue
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return invalid when latitude is invalid`() {
        // Given
        val latitude = 91.0

        // When
        val result = validator(
            id = "WH-123456",
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = latitude,
            longitude = 35.93
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "latitude",
                        error = ErrorValidator.InvalidLatitude
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return invalid when longitude is invalid`() {
        // Given
        val longitude = 181.0

        // When
        val result = validator(
            id = "WH-123456",
            name = "Main Warehouse",
            regionalZone = "North",
            latitude = 31.95,
            longitude = longitude
        )

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "longitude",
                        error = ErrorValidator.InvalidLongitude
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return all validation errors when multiple fields are invalid`() {
        // Given
        val result = validator(
            id = "",
            name = "",
            regionalZone = "",
            latitude = 91.0,
            longitude = 181.0
        )

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val errors = (result as ResultValidator.Invalid).errors

        assertEquals(5, errors.size)

        assertEquals(
            FieldValidator("id", ErrorValidator.BlankId),
            errors[0]
        )

        assertEquals(
            FieldValidator("name", ErrorValidator.BlankValue),
            errors[1]
        )

        assertEquals(
            FieldValidator("regionalZone", ErrorValidator.BlankValue),
            errors[2]
        )

        assertEquals(
            FieldValidator("latitude", ErrorValidator.InvalidLatitude),
            errors[3]
        )

        assertEquals(
            FieldValidator("longitude", ErrorValidator.InvalidLongitude),
            errors[4]
        )
    }
}