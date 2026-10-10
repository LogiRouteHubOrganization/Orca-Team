package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Warehouse
import domain.usecase.AnalyzePackageDestinationDemandUseCase
import org.junit.jupiter.api.Test

class AnalyzePackageDestinationDemandUseCaseTest {

    private val useCase = AnalyzePackageDestinationDemandUseCase()

    @Test
    fun `returns warehouse with highest package destination demand`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val warehouseB = createWarehouse("WH-000002")
        val warehouseC = createWarehouse("WH-000003")

        val packages = listOf(
            createPackage(
                id = "PKG-000001",
                origin = originWarehouse,
                destination = warehouseB
            ),
            createPackage(
                id = "PKG-000002",
                origin = originWarehouse,
                destination = warehouseC
            ),
            createPackage(
                id = "PKG-000003",
                origin = originWarehouse,
                destination = warehouseB
            )
        )

        // When
        val result = useCase(packages)

        // Then
        assertThat(result).isEqualTo(warehouseB)
    }

    @Test
    fun `returns destination warehouse when list contains one package`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val packages = listOf(
            createPackage(
                id = "PKG-000001",
                origin = originWarehouse,
                destination = destinationWarehouse
            )
        )

        // When
        val result = useCase(packages)

        // Then
        assertThat(result).isEqualTo(destinationWarehouse)
    }
    @Test
    fun `returns first destination when warehouses have equal demand`() {
        // Given
        val originWarehouse = createWarehouse("WH-000001")
        val warehouseB = createWarehouse("WH-000002")
        val warehouseC = createWarehouse("WH-000003")

        val packages = listOf(
            createPackage(
                id = "PKG-000001",
                origin = originWarehouse,
                destination = warehouseB
            ),
            createPackage(
                id = "PKG-000002",
                origin = originWarehouse,
                destination = warehouseC
            )
        )

        // When
        val result = useCase(packages)

        // Then
        assertThat(result).isEqualTo(warehouseB)
    }

    @Test
    fun `returns null when package list is empty`() {
        // Given
        val packages = emptyList<Package>()

        // When
        val result = useCase(packages)

        // Then
        assertThat(result).isNull()
    }

    private fun createPackage(
        id: String,
        origin: Warehouse,
        destination: Warehouse
    ): Package {
        return Package(
            id = id,
            weightByKg = 10.0,
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

