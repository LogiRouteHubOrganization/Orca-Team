
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

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(
            listOf(startWarehouse, destinationWarehouse)
        )

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(
            listOf(startWarehouse, destinationWarehouse)
        )
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

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }
    }

    @Test
    fun `should choose shorter path when multiple paths exist`() = runTest {
        // Given
        val intermediateWarehouse = Warehouse(
            id = "WH-000003",
            name = "Intermediate Warehouse",
            regionalZone = "Central",
            latitude = 31.7,
            longitude = 34.9
        )

        val directRoute = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 20.0,
            typicalDelayMin = 5
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
            destinationWarehouse = destinationWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 4
        )

        startWarehouse.addRoute(directRoute)
        startWarehouse.addRoute(firstRoute)
        intermediateWarehouse.addRoute(secondRoute)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(
            listOf(
                startWarehouse,
                intermediateWarehouse,
                destinationWarehouse
            )
        )

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.getOrNull()).isEqualTo(
            listOf(
                startWarehouse,
                intermediateWarehouse,
                destinationWarehouse
            )
        )
    }

    @Test
    fun `should return path containing all warehouses in correct order`() = runTest {
        // Given
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

        val firstRoute = Route(
            id = "RT-000001",
            originWarehouse = startWarehouse,
            destinationWarehouse = secondWarehouse,
            distanceKm = 5.0,
            typicalDelayMin = 3
        )

        val secondRoute = Route(
            id = "RT-000002",
            originWarehouse = secondWarehouse,
            destinationWarehouse = thirdWarehouse,
            distanceKm = 7.0,
            typicalDelayMin = 4
        )

        val thirdRoute = Route(
            id = "RT-000003",
            originWarehouse = thirdWarehouse,
            destinationWarehouse = destinationWarehouse,
            distanceKm = 8.0,
            typicalDelayMin = 5
        )

        startWarehouse.addRoute(firstRoute)
        secondWarehouse.addRoute(secondRoute)
        thirdWarehouse.addRoute(thirdRoute)

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(
            listOf(
                startWarehouse,
                secondWarehouse,
                thirdWarehouse,
                destinationWarehouse
            )
        )

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

    @Test
    fun `should return empty list when destination is unreachable`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(
            listOf(startWarehouse, destinationWarehouse)
        )

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

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findAll()
        } returns Result.success(listOf(startWarehouse))

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000001"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(
            listOf(startWarehouse)
        )
    }

    @Test
    fun `should return failure when findAll warehouses fails`() = runTest {
        // Given
        val exception = RuntimeException("Failed to load warehouses")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            warehouseRepository.findAll()
        } returns Result.failure(exception)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }
}
