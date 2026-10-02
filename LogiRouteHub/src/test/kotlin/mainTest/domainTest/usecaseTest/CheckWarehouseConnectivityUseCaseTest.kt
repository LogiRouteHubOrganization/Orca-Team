package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.CheckWarehouseConnectivityUseCase
import domain.usecase.FindFewestHopsRouteUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class CheckWarehouseConnectivityUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val findFewestHopsRouteUseCase =
        mockk<FindFewestHopsRouteUseCase>()

    private val useCase = CheckWarehouseConnectivityUseCase(
        warehouseRepository = warehouseRepository,
        findFewestHopsRouteUseCase = findFewestHopsRouteUseCase
    )

    private val startWarehouse = Warehouse(
        id = "WH-000004",
        name = "Start Warehouse",
        regionalZone = "North",
        latitude = 31.9,
        longitude = 35.2
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000006",
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
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            findFewestHopsRouteUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        } returns Result.success(warehousePath)

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isTrue()

        coVerify(exactly = 1) {
            findFewestHopsRouteUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }
    }

    @Test
    fun `should return false when warehouses are not connected`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            findFewestHopsRouteUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        } returns Result.success(emptyList())

        // When
        val result = useCase(
            startWarehouseId = "WH-000001",
            destinationWarehouseId = "WH-000002"
        )

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isFalse()

        coVerify(exactly = 1) {
            findFewestHopsRouteUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }
    }

    @Test
    fun `should return false when start warehouse does not exist`() = runTest {
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
        assertThat(result.getOrNull()).isFalse()

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }

        coVerify(exactly = 0) {
            findFewestHopsRouteUseCase(any(), any())
        }
    }

    @Test
    fun `should return false when destination warehouse does not exist`() = runTest {
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
        assertThat(result.getOrNull()).isFalse()

        coVerify(exactly = 0) {
            findFewestHopsRouteUseCase(any(), any())
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

        coVerify(exactly = 0) {
            warehouseRepository.findById("WH-000002")
        }

        coVerify(exactly = 0) {
            findFewestHopsRouteUseCase(any(), any())
        }
    }

    @Test
    fun `should return failure when fewest hops route use case fails`() = runTest {
        // Given
        val exception = RuntimeException("Routing error")

        coEvery {
            warehouseRepository.findById("WH-000001")
        } returns Result.success(startWarehouse)

        coEvery {
            warehouseRepository.findById("WH-000002")
        } returns Result.success(destinationWarehouse)

        coEvery {
            findFewestHopsRouteUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
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
            findFewestHopsRouteUseCase(
                startWarehouseId = "WH-000001",
                destinationWarehouseId = "WH-000002"
            )
        }
    }
}
