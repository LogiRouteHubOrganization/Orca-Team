package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.usecase.AssignPackageToCargoQueueUseCase
import domain.usecase.SortingCargoQueueUseCase
import org.junit.jupiter.api.Test

class AssignPackageToCargoQueueUseCaseTest {

    private val sortingCargoQueueUseCase = SortingCargoQueueUseCase()

    private val useCase = AssignPackageToCargoQueueUseCase(
        sortCargoQueue = sortingCargoQueueUseCase
    )

    @Test
    fun `returns success when package is assigned`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val logisticsPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // When
        val result = useCase(logisticsPackage)

        // Then
        assertThat(result.isSuccess).isTrue()
    }

    @Test
    fun `adds package to empty cargo queue`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val logisticsPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // When
        useCase(logisticsPackage)

        // Then
        assertThat(originWarehouse.cargoQueue)
            .containsExactly(logisticsPackage)
    }

    @Test
    fun `keeps existing package when adding new package`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val existingPackage = createPackage(
            id = "PKG-000001",
            weight = 30.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        val newPackage = createPackage(
            id = "PKG-000002",
            weight = 10.0,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        originWarehouse.addPackage(existingPackage)

        // When
        useCase(newPackage)

        // Then
        assertThat(originWarehouse.cargoQueue)
            .containsExactly(existingPackage, newPackage)
    }

    @Test
    fun `sorts cargo queue after adding package`() {
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

        // When
        useCase(newPackage)

        // Then
        assertThat(originWarehouse.cargoQueue)
            .containsExactly(newPackage, existingPackage)
            .inOrder()
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

