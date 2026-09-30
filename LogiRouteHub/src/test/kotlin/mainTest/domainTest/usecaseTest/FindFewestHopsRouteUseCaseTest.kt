
package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.routing.LeastHopPathFinder
import domain.usecase.FindFewestHopsRouteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindFewestHopsRouteUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val pathFinder = mockk<LeastHopPathFinder>()

    private val useCase = FindFewestHopsRouteUseCase(
        warehouseRepository = warehouseRepository,
        pathFinder = pathFinder
    )

    private val startWarehouse = Warehouse(
        id = "WH-001",
        name = "Start Warehouse",
        regionalZone = "North",
        latitude = 31.9,
        longitude = 35.2
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-002",
        name = "Destination Warehouse",
        regionalZone = "South",
        latitude = 31.5,
        longitude = 34.5
    )

    @Test
    fun `should return least hop path when both warehouses exist`() = runTest {
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
            pathFinder.findLeastHopPath(
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
            pathFinder.findLeastHopPath(
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
            pathFinder.findLeastHopPath(any(), any())
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
            pathFinder.findLeastHopPath(any(), any())
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
            pathFinder.findLeastHopPath(any(), any())
        }
    }

    @Test
    fun `should return failure when path finder fails`() = runTest {
        // Given
        val exception = RuntimeException("Path finding error")

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            pathFinder.findLeastHopPath(
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
            pathFinder.findLeastHopPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
}

