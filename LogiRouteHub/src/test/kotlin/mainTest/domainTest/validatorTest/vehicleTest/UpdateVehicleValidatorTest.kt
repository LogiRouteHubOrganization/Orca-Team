package mainTest.domainTest.validatorTest.vehicleTest

import com.google.common.truth.Truth.assertThat
import domain.model.updateinput.VehicleUpdateModel
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.vehicle.UpdateVehicleValidator
import org.junit.jupiter.api.Test

class UpdateVehicleValidatorTest {

    @Test
    fun `should return invalid when update model is empty`() {
        // Given
        val updateModel = VehicleUpdateModel()

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "update",
                error = ErrorValidator.EmptyUpdate
            )
        )
    }

    @Test
    fun `should return valid when max capacity is positive`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxCapacityKg = 1500.0
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when max capacity is zero`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxCapacityKg = 0.0
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "maxCapacityKg",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return valid when max volume is positive`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxVolumeM3 = 15.0
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when max volume is zero`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxVolumeM3 = 0.0
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "maxVolumeM3",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return invalid when max volume is negative`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxVolumeM3 = -15.0
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "maxVolumeM3",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return valid when cost per km is positive`() {
        // Given
        val updateModel = VehicleUpdateModel(
            costPerKm = 2.5
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when cost per km is not positive`() {
        // Given
        val updateModel = VehicleUpdateModel(
            costPerKm = -3.0
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "costPerKm",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return valid when warehouse id is valid`() {
        // Given
        val updateModel = VehicleUpdateModel(
            currentWarehouseId = "WH-000001"
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when warehouse id has invalid prefix`() {
        // Given
        val updateModel = VehicleUpdateModel(
            currentWarehouseId = "WA-000001"
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "currentWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }

    @Test
    fun `should return valid when all update fields are valid`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxCapacityKg = 2000.0,
            maxVolumeM3 = 25.0,
            costPerKm = 4.0,
            currentWarehouseId = "WH-000001"
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return all validation errors when multiple update fields are invalid`() {
        // Given
        val updateModel = VehicleUpdateModel(
            maxCapacityKg = 0.0,
            maxVolumeM3 = -10.0,
            costPerKm = -5.0,
            currentWarehouseId = "ZA-001"
        )

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "maxCapacityKg",
                error = ErrorValidator.MustBePositive
            ),
            FieldValidator(
                field = "maxVolumeM3",
                error = ErrorValidator.MustBePositive
            ),
            FieldValidator(
                field = "costPerKm",
                error = ErrorValidator.MustBePositive
            ),
            FieldValidator(
                field = "currentWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }
}

