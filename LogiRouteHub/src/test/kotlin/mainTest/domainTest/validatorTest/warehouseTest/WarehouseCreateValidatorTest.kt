
package mainTest.domainTest.validatorTest.warehouseTest

import com.google.common.truth.Truth.assertThat
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.warehouse.CreateWarehouseValidator
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
        assertThat(result).isEqualTo(ResultValidator.Valid)
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "id",
                        error = ErrorValidator.BlankId
                    )
                )
            )
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "id",
                        error = ErrorValidator.InvalidIdPrefix(
                            allowedPrefixes = setOf("WH-")
                        )
                    )
                )
            )
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "id",
                        error = ErrorValidator.InvalidIdFormat
                    )
                )
            )
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "name",
                        error = ErrorValidator.BlankValue
                    )
                )
            )
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "regionalZone",
                        error = ErrorValidator.BlankValue
                    )
                )
            )
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "latitude",
                        error = ErrorValidator.InvalidLatitude
                    )
                )
            )
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "longitude",
                        error = ErrorValidator.InvalidLongitude
                    )
                )
            )
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
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).hasSize(5)

        assertThat(errors[0]).isEqualTo(
            FieldValidator("id", ErrorValidator.BlankId)
        )

        assertThat(errors[1]).isEqualTo(
            FieldValidator("name", ErrorValidator.BlankValue)
        )

        assertThat(errors[2]).isEqualTo(
            FieldValidator("regionalZone", ErrorValidator.BlankValue)
        )

        assertThat(errors[3]).isEqualTo(
            FieldValidator("latitude", ErrorValidator.InvalidLatitude)
        )

        assertThat(errors[4]).isEqualTo(
            FieldValidator("longitude", ErrorValidator.InvalidLongitude)
        )
    }
}

