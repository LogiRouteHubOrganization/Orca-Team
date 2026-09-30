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
    fun `should return EmptyUpdate when update model is empty`() {
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
    fun `should return Valid when maxCapacityKg is positive`() {
        // Given
        val updateModel = VehicleUpdateModel(maxCapacityKg = 1500.0)

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return MustBePositive when maxCapacityKg is not positive`() {
        // Given
        val updateModel = VehicleUpdateModel(maxCapacityKg = 0.0)

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
    fun `should return Valid when costPerKm is positive`() {
        // Given
        val updateModel = VehicleUpdateModel(costPerKm = 2.5)

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return MustBePositive when costPerKm is not positive`() {
        // Given
        val updateModel = VehicleUpdateModel(costPerKm = -3.0)

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
    fun `should return Valid when warehouseId is valid`() {
        // Given
        val updateModel = VehicleUpdateModel(currentWarehouseId = "WH-000001")

        // When
        val result = UpdateVehicleValidator().invoke(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return InvalidIdPrefix when warehouseId has invalid prefix`() {
        // Given
        val updateModel = VehicleUpdateModel(currentWarehouseId = "WA-000001")

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
    fun `should return Valid when all update fields are valid`(){
        // Given
        val updateModel = VehicleUpdateModel(
            maxCapacityKg = 2000.0,
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
