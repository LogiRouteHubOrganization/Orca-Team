
package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.routing.ShortestPathFinder
import domain.usecase.FindOptimalPathUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindOptimalPathUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val shortestPathFinder = mockk<ShortestPathFinder>()

    private val useCase = FindOptimalPathUseCase(
        warehouseRepository = warehouseRepository,
        shortestPathFinder = shortestPathFinder
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
    fun `should return shortest path when both warehouses exist`() = runTest {
        // Given
        val expectedPath = listOf(
            startWarehouse,
            destinationWarehouse
        )

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            shortestPathFinder.findShortestPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } returns expectedPath

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(expectedPath)

        coVerify(exactly = 1) {
            shortestPathFinder.findShortestPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }

    @Test
    fun `should return empty list when start warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(null)

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-002")
        }

        coVerify(exactly = 0) {
            shortestPathFinder.findShortestPath(any(), any())
        }
    }

    @Test
    fun `should return empty list when destination warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-002")
        } returns Result.success(null)

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEmpty()

        coVerify(exactly = 0) {
            shortestPathFinder.findShortestPath(any(), any())
        }
    }

    @Test
    fun `should return failure when warehouse repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.failure(exception)

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-002")
        }

        coVerify(exactly = 0) {
            shortestPathFinder.findShortestPath(any(), any())
        }
    }

    @Test
    fun `should return failure when shortest path finder fails`() = runTest {
        // Given
        val exception = RuntimeException("Path finding error")

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            shortestPathFinder.findShortestPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } throws exception

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)

        coVerify(exactly = 1) {
            shortestPathFinder.findShortestPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
}
