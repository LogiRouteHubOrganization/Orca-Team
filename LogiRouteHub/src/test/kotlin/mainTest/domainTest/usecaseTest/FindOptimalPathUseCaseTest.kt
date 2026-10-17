package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Route
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.FindOptimalPathUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class FindOptimalPathUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()

    private val useCase = FindOptimalPathUseCase(
        warehouseRepository = warehouseRepository
    )

    private val startWarehouse = Warehouse(
        id = "WH-000001", name = "Start Warehouse", regionalZone = "North", latitude = 31.9, longitude = 35.2
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000002", name = "Destination Warehouse", regionalZone = "South", latitude = 31.5, longitude = 34.5
    )


    @Test
    fun `should return success result when both warehouses exist`() = runTest {
        stubSuccessfulPathResolution()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should return correct shortest path list when both warehouses exist`() = runTest {
        stubSuccessfulPathResolution()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isEqualTo(
            listOf(startWarehouse, destinationWarehouse)
        )
    }


    @Test
    fun `should return empty list when start warehouse does not exist`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(null)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `should skip finding destination warehouse when start warehouse does not exist`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(null)

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }
    }


    @Test
    fun `should return success result when destination warehouse does not exist`() = runTest {
        stubMissingDestinationWarehouse()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should return empty list when destination warehouse does not exist`() = runTest {
        stubMissingDestinationWarehouse()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isEmpty()
    }


    @Test
    fun `should return failure result when findById for start warehouse fails`() = runTest {
        val exception = RuntimeException("Repository error")
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.failure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should propagate exact exception when findById for start warehouse fails`() = runTest {
        val exception = RuntimeException("Repository error")
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.failure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `should skip finding destination warehouse when start warehouse fetch fails`() = runTest {
        val exception = RuntimeException("Repository error")
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.failure(exception)

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }
    }

    @Test
    fun `should return failure result when findAll warehouses fails`() = runTest {
        val exception = RuntimeException("Failed to load warehouses")
        stubFindAllFailure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should propagate exact exception when findAll warehouses fails`() = runTest {
        val exception = RuntimeException("Failed to load warehouses")
        stubFindAllFailure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }


    @Test
    fun `should choose path with shortest total distance when multiple paths exist`() = runTest {
        // Given
        setupShorterPathScenario()

        // When
        val result = useCase("WH-000001", "WH-000002")

        // Then
        assertThat(result.getOrNull()).isEqualTo(
            listOf(
                startWarehouse,
                intermediateWarehouse,
                destinationWarehouse
            )
        )
    }

    private fun setupShorterPathScenario() {

        startWarehouse.addRoute(Route("RT-000001", startWarehouse, destinationWarehouse, 20.0, 5))
        startWarehouse.addRoute(Route("RT-000002", startWarehouse, intermediateWarehouse, 5.0, 3))
        intermediateWarehouse.addRoute(Route("RT-000003", intermediateWarehouse, destinationWarehouse, 5.0, 4))

        coEvery { warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery { warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery { warehouseRepository.findAll()
        } returns Result.success(
            listOf(startWarehouse, intermediateWarehouse, destinationWarehouse)
        )
    }

    private val intermediateWarehouse = Warehouse(
        id = "WH-000003",
        name = "Intermediate Warehouse",
        regionalZone = "Central",
        latitude = 31.7,
        longitude = 34.9
    )

    @Test
    fun `should return path containing all warehouses in correct order`() = runTest {
        val secondWarehouse = Warehouse(
            id = "WH-000003", name = "Second Warehouse", regionalZone = "Central", latitude = 31.7, longitude = 35.0
        )

        val thirdWarehouse = Warehouse(
            id = "WH-000004", name = "Third Warehouse", regionalZone = "West", latitude = 31.6, longitude = 34.8
        )

        val firstRoute = Route("RT-000001", startWarehouse, secondWarehouse, 5.0, 3)
        val secondRoute = Route("RT-000002", secondWarehouse, thirdWarehouse, 7.0, 4)
        val thirdRoute = Route("RT-000003", thirdWarehouse, destinationWarehouse, 8.0, 5)

        startWarehouse.addRoute(firstRoute)
        secondWarehouse.addRoute(secondRoute)
        thirdWarehouse.addRoute(thirdRoute)

        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { warehouseRepository.findAll() } returns Result.success(
            listOf(startWarehouse, secondWarehouse, thirdWarehouse, destinationWarehouse)
        )

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).containsExactly(
            startWarehouse, secondWarehouse, thirdWarehouse, destinationWarehouse
        ).inOrder()
    }

    @Test
    fun `should return empty list when destination is unreachable`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { warehouseRepository.findAll() } returns Result.success(
            listOf(startWarehouse, destinationWarehouse)
        )

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `should return start warehouse when start and destination are identical`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findAll() } returns Result.success(listOf(startWarehouse))

        val result = useCase("WH-000001", "WH-000001")

        assertThat(result.getOrNull()).isEqualTo(listOf(startWarehouse))
    }


    private fun stubSuccessfulPathResolution() {
        val route = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 10.0,
            typicalDelayMin = 5
        )
        startWarehouse.addRoute(route)

        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { warehouseRepository.findAll() } returns Result.success(listOf(startWarehouse, destinationWarehouse))
    }

    private fun stubMissingDestinationWarehouse() {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(null)
    }

    private fun stubFindAllFailure(exception: Exception) {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { warehouseRepository.findAll() } returns Result.failure(exception)
    }
}


