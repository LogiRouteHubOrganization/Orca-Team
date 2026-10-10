
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
            warehouseRepository.findById("WH-000001")
        }

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }
    }

    @Test
    fun `should choose path with fewer hops when multiple paths exist`() = runTest {
        // Given
        val intermediateWarehouse = Warehouse(
            id = "WH-000003",
            name = "Intermediate Warehouse",
            regionalZone = "Central",
            latitude = 31.7,
            longitude = 35.0
        )

        val farWarehouse = Warehouse(
            id = "WH-000004",
            name = "Far Warehouse",
            regionalZone = "West",
            latitude = 31.6,
            longitude = 34.8
        )

        val directRoute = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 100.0,
            typicalDelayMin = 10
        )

        val firstRoute = Route(
            id = "RT-000002",
            originWarehouse = startWarehouse,
            destinationWarehouse = intermediateWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        val secondRoute = Route(
            id = "RT-000003",
            originWarehouse = intermediateWarehouse,
            destinationWarehouse = farWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        val thirdRoute = Route(
            id = "RT-000004",
            originWarehouse = farWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        startWarehouse.addRoute(directRoute)
        startWarehouse.addRoute(firstRoute)
        intermediateWarehouse.addRoute(secondRoute)
        farWarehouse.addRoute(thirdRoute)

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
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            destinationWarehouse
        ).inOrder()
    }

    @Test
    fun `should return path with correct order when multiple hops are required`() = runTest {
        val (secondWarehouse, thirdWarehouse) = setupMultiHopScenario()

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            secondWarehouse,
            thirdWarehouse,
            destinationWarehouse
        ).inOrder()
    }

    private fun setupMultiHopScenario(): Pair<Warehouse, Warehouse> {
        val secondWarehouse = Warehouse(
            id = "WH-000003",
            name = "Second Warehouse",
            regionalZone = "Central",
            latitude = 31.7,
            longitude = 35.0
        )
        val thirdWarehouse = Warehouse(
            id = "WH-000004",
            name = "Third Warehouse",
            regionalZone = "West",
            latitude = 31.6,
            longitude = 34.8
        )

        startWarehouse.addRoute(Route(
            "RT-000001",
            startWarehouse,
            secondWarehouse,
            5.0, 3))
        secondWarehouse.addRoute(Route(
            "RT-000002",
            secondWarehouse,
            thirdWarehouse,
            7.0, 4))
        thirdWarehouse.addRoute(Route(
            "RT-000003",
            thirdWarehouse,
            destinationWarehouse,
            8.0, 5))

        coEvery { warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery { warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        return Pair(secondWarehouse, thirdWarehouse)
    }

    @Test
    fun `should return empty list when destination is unreachable`() = runTest {
        // Given
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
        assertThat(result.getOrNull()).isEmpty()
    }

    @Test
    fun `should return start warehouse when start and destination are the same`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000001"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse
        )
    }

    @Test
    fun `should handle cycle without infinite traversal`() = runTest {
        // Given
        val middleWarehouse = Warehouse(
            id = "WH-000003",
            name = "Middle Warehouse",
            regionalZone = "Central",
            latitude = 31.7,
            longitude = 35.0
        )

        val startToMiddle = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = middleWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        val middleToStart = Route(
            id = "RT-000002",
            originWarehouse = middleWarehouse,
            destinationWarehouse = startWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        val middleToDestination = Route(
            id = "RT-000003",
            originWarehouse = middleWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        startWarehouse.addRoute(startToMiddle)
        middleWarehouse.addRoute(middleToStart)
        middleWarehouse.addRoute(middleToDestination)

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
        assertThat(result.getOrNull()).containsExactly(
            startWarehouse,
            middleWarehouse,
            destinationWarehouse
        ).inOrder()
    }
}

