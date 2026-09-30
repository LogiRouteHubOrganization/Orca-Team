package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.routing.LeastHopRouter
import domain.usecase.CheckWarehouseConnectivityUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CheckWarehouseConnectivityUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val leastHopRouter = mockk<LeastHopRouter>()

    private val useCase = CheckWarehouseConnectivityUseCase(
        warehouseRepository = warehouseRepository,
        leastHopRouter = leastHopRouter
    )

    private val startWarehouse = Warehouse(
        id = "WH-001234",
        name = "Start Warehouse",
        regionalZone = "North",
        latitude = 31.9,
        longitude = 35.2
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-002345",
        name = "Destination Warehouse",
        regionalZone = "South",
        latitude = 31.5,
        longitude = 34.5
    )

    @Test
    fun `should return true when warehouses are connected`() = runTest {
        // Given
        val warehousePath = listOf(
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
            leastHopRouter.findLeastHopPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } returns warehousePath

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isTrue()

        coVerify(exactly = 1) {
            leastHopRouter.findLeastHopPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }

    @Test
    fun `should return false when warehouses are not connected`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            leastHopRouter.findLeastHopPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } returns emptyList()

        // When
        val result = useCase(
            startWarehouseId = "WH-001",
            destinationWarehouseId = "WH-002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isFalse()

        coVerify(exactly = 1) {
            leastHopRouter.findLeastHopPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }

    @Test
    fun `should return false when start warehouse does not exist`() = runTest {
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
        assertThat(result.getOrNull()).isFalse()

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-002")
        }

        coVerify(exactly = 0) {
            leastHopRouter.findLeastHopPath(any(), any())
        }
    }

    @Test
    fun `should return false when destination warehouse does not exist`() = runTest {
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
        assertThat(result.getOrNull()).isFalse()

        coVerify(exactly = 0) {
            leastHopRouter.findLeastHopPath(any(), any())
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
            leastHopRouter.findLeastHopPath(any(), any())
        }
    }

    @Test
    fun `should return failure when router fails`() = runTest {
        // Given
        val exception = RuntimeException("Routing error")

        coEvery {
            warehouseRepository.findById("WH-001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            leastHopRouter.findLeastHopPath(
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
            leastHopRouter.findLeastHopPath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
}