package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.usecase.SortingCargoQueueUseCase
import org.junit.jupiter.api.Test

class SortingCargoQueueUseCaseTest {

    private val useCase = SortingCargoQueueUseCase()

    @Test
    fun `sorts cargo queue by package weight in descending order`() {
        // Given
        val warehouse = createWarehouse("WH-000001")
        val destination = createWarehouse("WH-000002")

        val lightPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = warehouse,
            destination = destination
        )
        val heavyPackage = createPackage(
            id = "PKG-000002",
            weight = 30.0,
            origin = warehouse,
            destination = destination
        )
        val mediumPackage = createPackage(
            id = "PKG-000003",
            weight = 20.0,
            origin = warehouse,
            destination = destination
        )
        warehouse.addPackage(lightPackage)
        warehouse.addPackage(heavyPackage)
        warehouse.addPackage(mediumPackage)

        // When
        useCase(warehouse)

        // Then
        assertThat(warehouse.cargoQueue.map { it.weightByKg }).containsExactly(
            30.0,
            20.0,
            10.0
        ).inOrder()
    }

    @Test
    fun `keeps cargo queue unchanged when already sorted`() {
        // Given
        val warehouse = createWarehouse("WH-000001")
        val destination = createWarehouse("WH-000002")

        val heavyPackage = createPackage(
            id = "PKG-000001",
            weight = 30.0,
            origin = warehouse,
            destination = destination
        )
        val mediumPackage = createPackage(
            id = "PKG-000002",
            weight = 20.0,
            origin = warehouse,
            destination = destination
        )
        val lightPackage = createPackage(
            id = "PKG-000003",
            weight = 10.0,
            origin = warehouse,
            destination = destination
        )
        warehouse.addPackage(heavyPackage)
        warehouse.addPackage(mediumPackage)
        warehouse.addPackage(lightPackage)

        // When
        useCase(warehouse)

        // Then
        assertThat(
            warehouse.cargoQueue.map { it.weightByKg }
        ).containsExactly(
            30.0,
            20.0,
            10.0
        ).inOrder()
    }

    @Test
    fun `keeps single package unchanged`() {
        // Given
        val warehouse = createWarehouse("WH-000001")
        val destination = createWarehouse("WH-000002")

        val logisticsPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = warehouse,
            destination = destination
        )

        warehouse.addPackage(logisticsPackage)

        // When
        useCase(warehouse)

        // Then
        assertThat(warehouse.cargoQueue)
            .containsExactly(logisticsPackage)
            .inOrder()
    }

    @Test
    fun `keeps empty cargo queue unchanged`() {
        // Given
        val warehouse = createWarehouse("WH-000001")

        // When
        useCase(warehouse)

        // Then
        assertThat(warehouse.cargoQueue).isEmpty()
    }

    @Test
    fun `keeps all packages when packages have equal weights`() {
        // Given
        val warehouse = createWarehouse("WH-000001")
        val destination = createWarehouse("WH-000002")

        val firstPackage = createPackage(
            id = "PKG-000001",
            weight = 10.0,
            origin = warehouse,
            destination = destination
        )

        val secondPackage = createPackage(
            id = "PKG-000002",
            weight = 10.0,
            origin = warehouse,
            destination = destination
        )

        warehouse.addPackage(firstPackage)
        warehouse.addPackage(secondPackage)

        // When
        useCase(warehouse)

        // Then
        assertThat(warehouse.cargoQueue)
            .containsExactly(
                firstPackage,
                secondPackage
            )
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

