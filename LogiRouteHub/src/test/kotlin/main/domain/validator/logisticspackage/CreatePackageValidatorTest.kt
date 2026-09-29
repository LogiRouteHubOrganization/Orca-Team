package main.domain.validator.logisticspackage

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
    fun `returns valid when all package data is valid`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when package weight is zero`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 0.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "weightByKg",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `returns invalid when package weight is negative`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = -5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "weightByKg",
                error = ErrorValidator.MustBePositive
            )
        )
    }

    @Test
    fun `returns invalid when package priority is not allowed`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            priority = "HIGH",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "priority",
                error = ErrorValidator.InvalidPriority(
                    allowedPriorities = setOf(
                        "STANDARD",
                        "LOW",
                        "URGENT"
                    )
                )
            )
        )
    }

    @Test
    fun `returns invalid when package id has invalid prefix`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "ABC-000001",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("PKG-")
                )
            )
        )
    }

    @Test
    fun `returns invalid when package id has invalid format`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "PKG-123",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdFormat
            )
        )
    }

    @Test
    fun `returns invalid when origin and destination warehouses are the same`() {
        // Given
        val warehouse = createWarehouse("WH-000001")

        // When
        val result = validator(
            id = "PKG-000001",
            weightByKg = 10.0,
            priority = "URGENT",
            originWarehouse = warehouse,
            destinationWarehouse = warehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "values",
                error = ErrorValidator.DifferentLocations
            )
        )
    }

    @Test
    fun `returns all validation errors when multiple package fields are invalid`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        // When
        val result = validator(
            id = "BAD-000001",
            weightByKg = 0.0,
            priority = "HIGH",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "id",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("PKG-")
                )
            )
        )

        assertThat(errors).contains(
            FieldValidator(
                field = "weightByKg",
                error = ErrorValidator.MustBePositive
            )
        )

        assertThat(errors).contains(
            FieldValidator(
                field = "priority",
                error = ErrorValidator.InvalidPriority(
                    allowedPriorities = setOf(
                        "STANDARD",
                        "LOW",
                        "URGENT"
                    )
                )
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
