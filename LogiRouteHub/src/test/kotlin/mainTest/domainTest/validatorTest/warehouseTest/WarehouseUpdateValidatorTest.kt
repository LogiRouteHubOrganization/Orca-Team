package mainTest.domainTest.validatorTest.warehouseTest

import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.warehouse.UpdateWarehouseValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WarehouseUpdateValidatorTest {

    private val validator = UpdateWarehouseValidator()

    @Test
    fun `should return invalid when no fields are provided for update`() {
        // Given
        val updateModel = WarehouseUpdateModel()

        // When
        val result = validator(updateModel)

        // Then
        assertEquals(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "update",
                        error = ErrorValidator.EmptyUpdate
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `should return valid when name is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            name = "Updated Warehouse"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return invalid when name is blank`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            name = "   "
        )

        // When
        val result = validator(updateModel)

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
    fun `should return valid when regional zone is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            regionalZone = "South"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return invalid when regional zone is blank`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            regionalZone = ""
        )

        // When
        val result = validator(updateModel)

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
    fun `should return valid when latitude is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            latitude = 31.95
        )

        // When
        val result = validator(updateModel)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return invalid when latitude is invalid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            latitude = 91.0
        )

        // When
        val result = validator(updateModel)

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
    fun `should return valid when longitude is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            longitude = 35.93
        )

        // When
        val result = validator(updateModel)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return invalid when longitude is invalid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            longitude = 181.0
        )

        // When
        val result = validator(updateModel)

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
    fun `should return valid when multiple valid fields are provided`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            name = "Updated Warehouse",
            regionalZone = "South",
            latitude = 31.95,
            longitude = 35.93
        )

        // When
        val result = validator(updateModel)

        // Then
        assertEquals(ResultValidator.Valid, result)
    }

    @Test
    fun `should return all validation errors when multiple fields are invalid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            name = "",
            regionalZone = "   ",
            latitude = 91.0,
            longitude = 181.0
        )

        // When
        val result = validator(updateModel)

        // Then
        assertTrue(result is ResultValidator.Invalid)

        val errors = (result as ResultValidator.Invalid).errors

        assertEquals(4, errors.size)

        assertEquals(
            FieldValidator("name", ErrorValidator.BlankValue),
            errors[0]
        )

        assertEquals(
            FieldValidator("regionalZone", ErrorValidator.BlankValue),
            errors[1]
        )

        assertEquals(
            FieldValidator("latitude", ErrorValidator.InvalidLatitude),
            errors[2]
        )

        assertEquals(
            FieldValidator("longitude", ErrorValidator.InvalidLongitude),
            errors[3]
        )
    }
}