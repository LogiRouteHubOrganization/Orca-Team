package mainTest.domainTest.validatorTest.logisticspackageTest

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.logisticspackage.CreatePackageValidator
import org.junit.jupiter.api.Test

class CreatePackageValidatorTest {

    private val validator = CreatePackageValidator()

    @Test
    fun `should return valid when all package data is valid`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            volumeM3 = 2.5,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when package weight is zero`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 0.0,
            volumeM3 = 2.5,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "weightByKg",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return invalid when package weight is negative`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = -10.0,
            volumeM3 = 2.5,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "weightByKg",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return invalid when package volume is zero`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            volumeM3 = 0.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "volumeM3",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return invalid when package volume is negative`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            volumeM3 = -2.5,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "volumeM3",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `should return invalid when package priority is not allowed`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            volumeM3 = 2.5,
            priority = "HIGH",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "priority",
                error = ErrorValidator.InvalidPriority(
                    allowedPriorities = setOf("URGENT", "STANDARD", "LOW")
                )
            )
        )
    }

    @Test
    fun `should return invalid when package id has invalid prefix`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "ABC-000001",
            weightByKg = 10.0,
            volumeM3 = 2.5,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(
            invalidResult.errors.any { it.field == "id" }
        ).isTrue()
    }

    @Test
    fun `should return invalid when package id has invalid format`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-ABC",
            weightByKg = 10.0,
            volumeM3 = 2.5,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(
            invalidResult.errors.any { it.field == "id" }
        ).isTrue()
    }

    @Test
    fun `should return invalid when origin and destination warehouses are the same`() {
        // Given
        val warehouse = createWarehouse("WH-000001")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            volumeM3 = 2.5,
            priority = "URGENT",
            originWarehouse = warehouse,
            destinationWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(
            invalidResult.errors.any { it.field == "values" }
        ).isTrue()
    }

    @Test
    fun `should return all validation errors when multiple package fields are invalid`() {
        // Given
        val warehouse = createWarehouse("WH-000001")

        // When
        val result = validator(
            id = "ABC-000001",
            weightByKg = -10.0,
            volumeM3 = -2.5,
            priority = "HIGH",
            originWarehouse = warehouse,
            destinationWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "weightByKg",
                error = ErrorValidator.MustBePositive
            )
        )

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "volumeM3",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    private fun createWarehouse(
        id: String,
        name: String = "Test Warehouse"
    ): Warehouse {
        return Warehouse(
            id = id,
            name = name,
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }
}