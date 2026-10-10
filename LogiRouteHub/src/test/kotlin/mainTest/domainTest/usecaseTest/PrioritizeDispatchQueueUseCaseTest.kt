package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.model.request.PackagePriorityLevels
import domain.repository.WarehouseRepository
import domain.usecase.PrioritizeDispatchQueueUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PrioritizeDispatchQueueUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()

    private val priorityLevels = PackagePriorityLevels(
        urgent = 3,
        standard = 2,
        low = 1,
        unknown = 0
    )

    private val useCase = PrioritizeDispatchQueueUseCase(
        warehouseRepository = warehouseRepository,
        priorityLevels = priorityLevels
    )

    @Test
    fun `prioritizes packages by priority and then by weight`() = runTest {
        // Given
        val warehouse = createWarehouse()

        warehouse.addPackage(
            packageOf(
                id = "PKG-000001",
                priority = "LOW",
                weight = 20.0,
                originWarehouse = warehouse
            )
        )

        warehouse.addPackage(
            packageOf(
                id = "PKG-000002",
                priority = "URGENT",
                weight = 10.0,
                originWarehouse = warehouse
            )
        )

        warehouse.addPackage(
            packageOf(
                id = "PKG-000003",
                priority = "URGENT",
                weight = 30.0,
                originWarehouse = warehouse
            )
        )

        warehouse.addPackage(
            packageOf(
                id = "PKG-000004",
                priority = "STANDARD",
                weight = 50.0,
                originWarehouse = warehouse
            )
        )

        coEvery {
            warehouseRepository.findById("WH-123456")
        } returns Result.success(warehouse)

        // When
        val result = useCase("WH-123456")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(warehouse)

        assertThat(warehouse.cargoQueue.map { it.id })
            .containsExactly(
                "PKG-000003",
                "PKG-000002",
                "PKG-000004",
                "PKG-000001"
            )
            .inOrder()
    }

    @Test
    fun `sorts packages by descending weight when they have the same priority`() = runTest {
        // Given
        val warehouse = createWarehouse()

        warehouse.addPackage(
            packageOf(
                id = "PKG-000001",
                priority = "STANDARD",
                weight = 10.0,
                originWarehouse = warehouse
            )
        )

        warehouse.addPackage(
            packageOf(
                id = "PKG-000002",
                priority = "STANDARD",
                weight = 40.0,
                originWarehouse = warehouse
            )
        )

        warehouse.addPackage(
            packageOf(
                id = "PKG-000003",
                priority = "STANDARD",
                weight = 25.0,
                originWarehouse = warehouse
            )
        )

        coEvery {
            warehouseRepository.findById("WH-123456")
        } returns Result.success(warehouse)

        // When
        val result = useCase("WH-123456")

        // Then
        assertThat(result.isSuccess).isTrue()

        assertThat(warehouse.cargoQueue.map { it.id })
            .containsExactly(
                "PKG-000002",
                "PKG-000003",
                "PKG-000001"
            )
            .inOrder()
    }

    @Test
    fun `returns null when warehouse does not exist`() = runTest {
        // Given
        coEvery {
            warehouseRepository.findById("WH-123456")
        } returns Result.success(null)

        // When
        val result = useCase("WH-123456")

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNull()

        coVerify(exactly = 1) {
            warehouseRepository.findById("WH-123456")
        }
    }

    @Test
    fun `returns failure when warehouse repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Database error")

        coEvery {
            warehouseRepository.findById("WH-123456")
        } returns Result.failure(exception)

        // When
        val result = useCase("WH-123456")

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(exception)
    }

    private fun createWarehouse(): Warehouse {
        return Warehouse(
            id = "WH-123456",
            name = "Test Warehouse",
            regionalZone = "NORTH",
            latitude = 32.0,
            longitude = 35.0
        )
    }

    private fun createDestinationWarehouse(): Warehouse {
        return Warehouse(
            id = "WH-654321",
            name = "Destination Warehouse",
            regionalZone = "SOUTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }

    private fun packageOf(
        id: String,
        priority: String,
        weight: Double,
        originWarehouse: Warehouse
    ): Package {
        return Package(
            id = id,
            weightByKg = weight,
            priority = priority,
            originWarehouse = originWarehouse,
            destinationWarehouse = createDestinationWarehouse()
        )
    }
}
