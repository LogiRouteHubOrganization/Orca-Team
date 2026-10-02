package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Route
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.FindFewestHopsRouteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindFewestHopsRouteUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()

    private val useCase = FindFewestHopsRouteUseCase(
        warehouseRepository = warehouseRepository
    )

    private val startWarehouse = Warehouse(
        id = "WH-000001",
        name = "Start Warehouse",
        regionalZone = "North",
        latitude = 31.9,
        longitude = 35.2
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000002",
        name = "Destination Warehouse",
        regionalZone = "South",
        latitude = 31.5,
        longitude = 34.5
    )

    @Test
    fun `should return least hop path when both warehouses exist`() = runTest {
        // Given
        val route = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 10.0,
            typicalDelayMin = 5
        )

        startWarehouse.addRoute(route)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            destinationWarehouse
        ).inOrder()
    }

    @Test
    fun `should return empty list when start warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(null)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }
    }

    @Test
    fun `should return empty list when destination warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(null)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000001")
        }

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-000002")
        }
    }

    @Test
    fun `should return failure when warehouse repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.failure(exception)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-001")
        }

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-002")
        }
    }
}
