
package mainTest.domainTest.validatorTest.warehouseTest

import com.google.common.truth.Truth.assertThat
import domain.model.updateinput.WarehouseUpdateModel
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.warehouse.UpdateWarehouseValidator
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
        assertThat(result).isEqualTo(
            ResultValidator.Invalid(
                listOf(
                    FieldValidator(
                        field = "update",
                        error = ErrorValidator.EmptyUpdate
                    )
                )
            )
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
        assertThat(result).isEqualTo(ResultValidator.Valid)
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
    fun `should return valid when regional zone is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            regionalZone = "South"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
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
    fun `should return valid when latitude is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            latitude = 31.95
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
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
    fun `should return valid when longitude is valid`() {
        // Given
        val updateModel = WarehouseUpdateModel(
            longitude = 35.93
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
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
        assertThat(result).isEqualTo(ResultValidator.Valid)
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
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).hasSize(4)

        assertThat(errors[0]).isEqualTo(
            FieldValidator("name", ErrorValidator.BlankValue)
        )

        assertThat(errors[1]).isEqualTo(
            FieldValidator("regionalZone", ErrorValidator.BlankValue)
        )

        assertThat(errors[2]).isEqualTo(
            FieldValidator("latitude", ErrorValidator.InvalidLatitude)
        )

        assertThat(errors[3]).isEqualTo(
            FieldValidator("longitude", ErrorValidator.InvalidLongitude)
        )
    }
}

