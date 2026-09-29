package main.domain.usecase.crud.warehouse

import com.google.common.truth.Truth.assertThat
import domain.exception.EntityValidationException
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.crud.warehouse.GetWarehouseByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetWarehouseByIdUseCaseTest {

    @Test
    fun `returns warehouse when id is valid and warehouse exists`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = GetWarehouseByIdUseCase(warehouseRepository)

        val warehouse = createWarehouse("WH-000001")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(warehouse)

        // When
        val result = useCase("WH-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(warehouse)

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }
    }

    @Test
    fun `returns null when id is valid but warehouse does not exist`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = GetWarehouseByIdUseCase(warehouseRepository)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(null)

        // When
        val result = useCase("WH-000001")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }
    }

    @Test
    fun `returns failure when repository find by id fails`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = GetWarehouseByIdUseCase(warehouseRepository)

        val exception = RuntimeException("Failed to get warehouse")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.failure(exception)

        // When
        val result = useCase("WH-000001")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }
    }

    @Test
    fun `returns validation failure when warehouse id is invalid`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()
        val useCase = GetWarehouseByIdUseCase(warehouseRepository)

        val invalidId = "ABC-000001"

        // When
        val result = useCase(invalidId)

        // Then
        assertThat(result.isFailure).isTrue()

        assertThat(result.exceptionOrNull())
            .isInstanceOf(EntityValidationException::class.java)

        coVerify(exactly = 0) {
            warehouseRepository.findById(any())
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