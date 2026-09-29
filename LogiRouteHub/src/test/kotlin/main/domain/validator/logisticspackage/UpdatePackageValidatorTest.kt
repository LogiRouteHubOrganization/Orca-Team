package main.domain.validator.logisticspackage

import com.google.common.truth.Truth.assertThat
import domain.model.updateinput.PackageUpdateModel
import domain.validator.ErrorValidator
import domain.validator.FieldValidator
import domain.validator.ResultValidator
import domain.validator.logisticspackage.UpdatePackageValidator
import org.junit.jupiter.api.Test

class UpdatePackageValidatorTest {

    private val validator = UpdatePackageValidator()

    @Test
    fun `returns valid when package weight update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 20.0
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when no package fields are provided for update`() {
        // Given
        val updateModel = PackageUpdateModel()

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "update",
                error = ErrorValidator.EmptyUpdate
            )
        )
    }

    @Test
    fun `returns invalid when updated package weight is zero`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 0.0
        )

        // When
        val result = validator(updateModel)

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
    fun `returns invalid when updated package weight is negative`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = -5.0
        )

        // When
        val result = validator(updateModel)

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
    fun `returns valid when package priority update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            priority = "URGENT"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when updated package priority is not allowed`() {
        // Given
        val updateModel = PackageUpdateModel(
            priority = "HIGH"
        )

        // When
        val result = validator(updateModel)

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
    fun `returns valid when origin warehouse id update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            originWarehouseId = "WH-000001"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when origin warehouse id has invalid prefix`() {
        // Given
        val updateModel = PackageUpdateModel(
            originWarehouseId = "ABC-000001"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "originWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }

    @Test
    fun `returns valid when destination warehouse id update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            destinationWarehouseId = "WH-000002"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns invalid when destination warehouse id has invalid prefix`() {
        // Given
        val updateModel = PackageUpdateModel(
            destinationWarehouseId = "ABC-000002"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

        assertThat(errors).contains(
            FieldValidator(
                field = "destinationWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }

    @Test
    fun `returns valid when multiple package updates are valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 25.0,
            priority = "STANDARD",
            originWarehouseId = "WH-000003",
            destinationWarehouseId = "WH-000004"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `returns all validation errors when multiple package updates are invalid`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 0.0,
            priority = "HIGH",
            originWarehouseId = "ABC-000001"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val errors = (result as ResultValidator.Invalid).errors

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
        assertThat(errors).contains(
            FieldValidator(
                field = "originWarehouseId",
                error = ErrorValidator.InvalidIdPrefix(
                    allowedPrefixes = setOf("WH-")
                )
            )
        )
    }

}
