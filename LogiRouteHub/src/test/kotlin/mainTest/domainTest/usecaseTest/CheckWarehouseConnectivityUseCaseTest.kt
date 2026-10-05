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
    private val findFewestHopsRouteUseCase = mockk<FindFewestHopsRouteUseCase>()

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
    fun `should return success result when warehouses are connected`() = runTest {
        stubConnectedWarehouses()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should return true value when warehouses are connected`() = runTest {
        stubConnectedWarehouses()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isTrue()
    }

    @Test
    fun `should invoke fewest hops route use case when warehouses exist`() = runTest {
        stubConnectedWarehouses()

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 1) {
            findFewestHopsRouteUseCase("WH-000001", "WH-000002")
        }
    }

    @Test
    fun `should return success result when warehouses are not connected`() = runTest {
        stubDisconnectedWarehouses()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `should return false value when warehouses are not connected`() = runTest {
        stubDisconnectedWarehouses()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isFalse()
    }


    @Test
    fun `should return false value when start warehouse does not exist`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(null)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isFalse()
    }

    @Test
    fun `should skip destination lookup when start warehouse does not exist`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(null)

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 0) { warehouseRepository.findById("WH-000002") }
    }

    @Test
    fun `should skip route calculation when start warehouse does not exist`() = runTest {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(null)

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 0) { findFewestHopsRouteUseCase(any(), any()) }
    }


    @Test
    fun `should return false value when destination warehouse does not exist`() = runTest {
        stubMissingDestinationWarehouse()

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.getOrNull()).isFalse()
    }

    @Test
    fun `should skip route calculation when destination warehouse does not exist`() = runTest {
        stubMissingDestinationWarehouse()

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 0) { findFewestHopsRouteUseCase(any(), any()) }
    }


    @Test
    fun `should return failure result when warehouse repository fails`() = runTest {
        val exception = RuntimeException("Repository error")
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.failure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should propagate exact exception when warehouse repository fails`() = runTest {
        val exception = RuntimeException("Repository error")
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.failure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    @Test
    fun `should skip route calculation when warehouse repository fails`() = runTest {
        val exception = RuntimeException("Repository error")
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.failure(exception)

        useCase("WH-000001", "WH-000002")

        coVerify(exactly = 0) { findFewestHopsRouteUseCase(any(), any()) }
    }


    @Test
    fun `should return failure result when route calculation fails`() = runTest {
        val exception = RuntimeException("Routing error")
        stubRouteCalculationFailure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `should propagate exact exception when route calculation fails`() = runTest {
        val exception = RuntimeException("Routing error")
        stubRouteCalculationFailure(exception)

        val result = useCase("WH-000001", "WH-000002")

        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }


    private fun stubConnectedWarehouses() {
        val warehousePath = listOf(startWarehouse, destinationWarehouse)
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { findFewestHopsRouteUseCase("WH-000001", "WH-000002") } returns Result.success(warehousePath)
    }

    private fun stubDisconnectedWarehouses() {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { findFewestHopsRouteUseCase("WH-000001", "WH-000002") } returns Result.success(emptyList())
    }

    private fun stubMissingDestinationWarehouse() {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(null)
    }

    private fun stubRouteCalculationFailure(exception: Exception) {
        coEvery { warehouseRepository.findById("WH-000001") } returns Result.success(startWarehouse)
        coEvery { warehouseRepository.findById("WH-000002") } returns Result.success(destinationWarehouse)
        coEvery { findFewestHopsRouteUseCase("WH-000001", "WH-000002") } returns Result.failure(exception)
    }
}