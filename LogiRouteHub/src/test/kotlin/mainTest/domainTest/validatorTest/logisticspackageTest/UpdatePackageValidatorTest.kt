package mainTest.domainTest.validatorTest.logisticspackageTest

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
    fun `should return valid when package weight update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 15.0
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return valid when package volume update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            volumeM3 = 3.5
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when no package fields are provided for update`() {
        // Given
        val updateModel = PackageUpdateModel()

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "update",
                error = ErrorValidator.EmptyUpdate
            )
        )
    }

    @Test
    fun `should return invalid when updated package weight is zero`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 0.0
        )

        // When
        val result = validator(updateModel)

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
    fun `should return invalid when updated package weight is negative`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = -10.0
        )

        // When
        val result = validator(updateModel)

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
    fun `should return invalid when updated package volume is zero`() {
        // Given
        val updateModel = PackageUpdateModel(
            volumeM3 = 0.0
        )

        // When
        val result = validator(updateModel)

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
    fun `should return invalid when updated package volume is negative`() {
        // Given
        val updateModel = PackageUpdateModel(
            volumeM3 = -3.5
        )

        // When
        val result = validator(updateModel)

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
    fun `should return valid when package priority update is valid`() {
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
    fun `should return invalid when updated package priority is not allowed`() {
        // Given
        val updateModel = PackageUpdateModel(
            priority = "HIGH"
        )

        // When
        val result = validator(updateModel)

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
    fun `should return valid when origin warehouse id update is valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            originWarehouseId = "WH-000002"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return invalid when origin warehouse id has invalid prefix`() {
        // Given
        val updateModel = PackageUpdateModel(
            originWarehouseId = "PKG-000002"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(
            invalidResult.errors.any { it.field == "originWarehouseId" }
        ).isTrue()
    }

    @Test
    fun `should return valid when destination warehouse id update is valid`() {
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
    fun `should return invalid when destination warehouse id has invalid prefix`() {
        // Given
        val updateModel = PackageUpdateModel(
            destinationWarehouseId = "PKG-000002"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isInstanceOf(ResultValidator.Invalid::class.java)

        val invalidResult = result as ResultValidator.Invalid

        assertThat(
            invalidResult.errors.any { it.field == "destinationWarehouseId" }
        ).isTrue()
    }

    @Test
    fun `should return valid when multiple package updates are valid`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = 15.0,
            volumeM3 = 3.5,
            priority = "STANDARD",
            originWarehouseId = "WH-000002",
            destinationWarehouseId = "WH-000003"
        )

        // When
        val result = validator(updateModel)

        // Then
        assertThat(result).isEqualTo(ResultValidator.Valid)
    }

    @Test
    fun `should return all validation errors when multiple package updates are invalid`() {
        // Given
        val updateModel = PackageUpdateModel(
            weightByKg = -15.0,
            volumeM3 = -3.5,
            priority = "HIGH",
            originWarehouseId = "PKG-000002",
            destinationWarehouseId = "PKG-000003"
        )

        // When
        val result = validator(updateModel)

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

        assertThat(invalidResult.errors).contains(
            FieldValidator(
                field = "priority",
                error = ErrorValidator.InvalidPriority(
                    allowedPriorities = setOf("URGENT", "STANDARD", "LOW")
                )
            )
        )

        assertThat(
            invalidResult.errors.any { it.field == "originWarehouseId" }
        ).isTrue()

        assertThat(
            invalidResult.errors.any { it.field == "destinationWarehouseId" }
        ).isTrue()
    }
}