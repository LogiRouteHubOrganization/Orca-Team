
package mainTest.domainTest.usecaseTest.crudTest.warehouse

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.crud.warehouse.CreateWarehouseUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CreateWarehouseUseCaseTest {

    @Test
    fun `returns created warehouse when repository add succeeds`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()

        val warehouse = Warehouse(
            id = "WH-000001",
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )

        coEvery {
            warehouseRepository.add(warehouse)
        } returns Result.success(warehouse)

        val useCase = CreateWarehouseUseCase(warehouseRepository)

        // When
        val result = useCase(warehouse)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(warehouse)

        coVerify(exactly = 1) {
            warehouseRepository.add(warehouse)
        }
    }

    @Test
    fun `returns failure when repository add fails`() = runTest {
        // Given
        val warehouseRepository = mockk<WarehouseRepository>()

        val warehouse = Warehouse(
            id = "WH-000001",
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )

        val exception = RuntimeException("Failed to create warehouse")

        coEvery {
            warehouseRepository.add(warehouse)
        } returns Result.failure(exception)

        val useCase = CreateWarehouseUseCase(warehouseRepository)

        // When
        val result = useCase(warehouse)

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            warehouseRepository.add(warehouse)
        }
    }
}