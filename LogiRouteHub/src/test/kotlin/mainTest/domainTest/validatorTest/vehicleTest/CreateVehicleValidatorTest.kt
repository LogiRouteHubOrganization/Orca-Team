package mainTest.domainTest.validatorTest.vehicleTest


import org.junit.jupiter.api.Test
import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.vehicle.CreateVehicleValidator
import io.mockk.every
import io.mockk.mockk

class CreateVehicleValidatorTest {

    @Test
    fun `returns valid when all vehicle data is valid`() {
        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = 1000.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )
        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)

    }

    @Test
    fun `returns blank id error when vehicle id is blank`() {

        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "",
            maxCapacityKg = 1000.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )
        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(field = "id",
                error = ErrorValidator.BlankId
            )
        )
    }

    @Test
    fun `returns invalid prefix error when vehicle id has invalid prefix`() {

        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "CAR-000001",
            maxCapacityKg = 1000.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("TRK-")
                )
            )
        )
    }

    @Test
    fun `returns invalid format error when vehicle id has invalid format`() {

        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "TRK-123",
            maxCapacityKg = 1000.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdFormat
            )
        )
    }

    @Test
    fun `negative max capacity should return MustBePositive error`() {
        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = -100.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

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
    fun `zero max capacity should return MustBePositive error`() {
        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = 0.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

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
    fun `negative cost per km should return MustBePositive error`() {
        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = 1000.0,
            costPerKm = -2.5,
            currentWarehouse = warehouse
        )

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
    fun `zero cost per km should return MustBePositive error`() {
        // Given
        val validator = CreateVehicleValidator()
        val warehouse = createValidWarehouse()

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = 1000.0,
            costPerKm = 0.0,
            currentWarehouse = warehouse
        )

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
    fun `invalid warehouse id prefix should return InvalidIdPrefix error`() {
        // Given
        val validator = CreateVehicleValidator()

        val warehouse = mockk<Warehouse>()
        every { warehouse.id } returns "TRK-000001"

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = 100.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

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
    fun `invalid warehouse id format should return InvalidIdFormat error`() {
        // Given
        val validator = CreateVehicleValidator()

        val warehouse = mockk<Warehouse>()
        every { warehouse.id } returns "WH-ABCDEF"

        // When
        val result = validator(
            id = "TRK-000001",
            maxCapacityKg = 100.0,
            costPerKm = 2.5,
            currentWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)
        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "currentWarehouseId",
                error = ErrorValidator.InvalidIdFormat
            )
        )
    }

    @Test
    fun `multiple invalid fields should return all validation errors`() {
        // Given
        val validator = CreateVehicleValidator()

        val warehouse = mockk<Warehouse>()
        every { warehouse.id } returns "TRK-ABCDEF"

        // When
        val result = validator(
            id = "TRK-ABCDEF",
            maxCapacityKg = -100.0,
            costPerKm = 0.0,
            currentWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)
        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).containsExactly(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdFormat
            ),
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






    private fun createValidWarehouse(
        id: String = "WH-000001"
    ): Warehouse {
        return Warehouse(
            id = id,
            name = "Main Warehouse",
            regionalZone = "Central",
            latitude = 31.5,
            longitude = 34.5
        )
    }
}

