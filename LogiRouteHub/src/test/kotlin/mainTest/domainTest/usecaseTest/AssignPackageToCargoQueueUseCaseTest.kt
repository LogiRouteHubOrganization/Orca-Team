package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.usecase.SortingCargoQueueUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AssignPackageToCargoQueueUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()

    private val sortingCargoQueueUseCase = SortingCargoQueueUseCase()

    private val useCase = AssignPackageToCargoQueueUseCase(
            sortCargoQueue = sortingCargoQueueUseCase,
            warehouseRepository = warehouseRepository
        )

    @Test
    fun `adds package to origin warehouse cargo queue`() = runTest {
        // Given
        val originWarehouse = createWarehouse("WH-000001")

        val destinationWarehouse = createWarehouse("WH-000002")

        val logisticsPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        coEvery {
            warehouseRepository.update(
                originWarehouse.id,
                any()
            )
        } returns Result.success(originWarehouse)

        // When
        useCase(logisticsPackage)

        // Then
        assertThat(originWarehouse.cargoQueue)
            .contains(logisticsPackage)
    }

    @Test
    fun `sorts cargo queue after adding package`() = runTest {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val existingPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )
        val newPackage = createPackage(
            id = "PKG-000002",
            weight = 30.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )
        originWarehouse.addPackage(existingPackage)

        coEvery {
            warehouseRepository.update(
                originWarehouse.id,
                any()
            )
        } returns Result.success(originWarehouse)

        // When
        useCase(newPackage)

        // Then
        assertThat(originWarehouse.cargoQueue.map {
                it.weightByKg
            }
        ).containsExactly(30.0, 10.0).inOrder()
    }

    @Test
    fun `updates origin warehouse repository after assigning package`() = runTest {

            // Given
            val originWarehouse = createWarehouse("WH-000001")

            val destinationWarehouse = createWarehouse("WH-000002")

            val logisticsPackage = createPackage(
                id = "PKG-000001",
                weight = 10.0,
                origin = originWarehouse,
                destination = destinationWarehouse
            )
            coEvery {
                warehouseRepository.update(
                    originWarehouse.id,
                    any()
                )
            } returns Result.success(originWarehouse)

            // When
            useCase(logisticsPackage)

            // Then
            coVerify(exactly = 1) {
                warehouseRepository.update(
                    originWarehouse.id,
                    any()
                )
            }
        }

    private fun createPackage(
        id: String,
        weight: Double,
        origin: Warehouse,
        destination: Warehouse
    ): Package {
        return Package(
            id = id,
            weightByKg = weight,
            priority = "STANDARD",
            originWarehouse = origin,
            destinationWarehouse = destination
        )
    }

    private fun createWarehouse(
        id: String,
        name: String = "Test Warehouse"
    ): Warehouse {
        return Warehouse(
            id = id,
            name = name,
            regionalZone = "NORTH",
            latitude = 31.5,
            longitude = 34.5
        )
    }
}
