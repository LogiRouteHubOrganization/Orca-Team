package main.domain.usecase.crud.warehouse

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository
import domain.usecase.crud.warehouse.UpdateWarehouseUseCase
import domain.validator.warehouse.UpdateWarehouseValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class UpdateWarehouseUseCaseTest {

    @Test
    fun `returns updated warehouse when id and update data are valid`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouseValidator = UpdateWarehouseValidator()

        val useCase = UpdateWarehouseUseCase(
            warehouseRepository = warehouseRepository,
            warehouseValidator = warehouseValidator
        )

        val updateModel = WarehouseUpdateModel(
            name = "Updated Warehouse"
        )

        val updatedWarehouse = Warehouse(
            id = "WH-000001",
            name = "Updated Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )

        coEvery {
            warehouseRepository.update(
                "WH-000001",
                updateModel
            )
        } returns Result.success(updatedWarehouse)

        // When
        val result = useCase(
            id = "WH-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(updatedWarehouse)

        coVerify(exactly = 1) {
            warehouseRepository.update(
                "WH-000001",
                updateModel
            )
        }
    }

    @Test
    fun `returns failure when repository update fails`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouseValidator = UpdateWarehouseValidator()

        val useCase = UpdateWarehouseUseCase(
            warehouseRepository = warehouseRepository,
            warehouseValidator = warehouseValidator
        )

        val updateModel = WarehouseUpdateModel(
            name = "Updated Warehouse"
        )

        val exception = RuntimeException("Failed to update warehouse")

        coEvery {
            warehouseRepository.update(
                "WH-000001",
                updateModel
            )
        } returns Result.failure(exception)

        // When
        val result = useCase(
            id = "WH-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            warehouseRepository.update(
                "WH-000001",
                updateModel
            )
        }
    }

    @Test
    fun `returns validation failure when warehouse id is invalid`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouseValidator = UpdateWarehouseValidator()

        val useCase = UpdateWarehouseUseCase(
            warehouseRepository = warehouseRepository,
            warehouseValidator = warehouseValidator
        )

        val updateModel = WarehouseUpdateModel(
            name = "Updated Warehouse"
        )

        // When
        val result = useCase(
            id = "ABC-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            warehouseRepository.update(any(), any())
        }
    }

    @Test
    fun `returns validation failure when update data is invalid`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouseValidator = UpdateWarehouseValidator()

        val useCase = UpdateWarehouseUseCase(
            warehouseRepository = warehouseRepository,
            warehouseValidator = warehouseValidator
        )

        val updateModel = WarehouseUpdateModel(
            name = ""
        )

        // When
        val result = useCase(
            id = "WH-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            warehouseRepository.update(any(), any())
        }
    }

    @Test
    fun `returns validation failure when update request is empty`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val warehouseValidator = UpdateWarehouseValidator()

        val useCase = UpdateWarehouseUseCase(
            warehouseRepository = warehouseRepository,
            warehouseValidator = warehouseValidator
        )

        val updateModel = WarehouseUpdateModel()

        // When
        val result = useCase(
            id = "WH-000001",
            updateRequest = updateModel
        )

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            warehouseRepository.update(any(), any())
        }
    }

    private fun createWarehouse(id: String): Warehouse {
        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }
}
