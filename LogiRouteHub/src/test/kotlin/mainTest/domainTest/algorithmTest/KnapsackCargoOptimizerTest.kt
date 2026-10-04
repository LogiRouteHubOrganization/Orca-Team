package mainTest.domainTest.algorithmTest

import com.google.common.truth.Truth.assertThat
import domain.algorithm.knapsack.KnapsackCargoOptimizer
import domain.model.Package
import domain.model.Warehouse
import org.junit.jupiter.api.Test

class KnapsackCargoOptimizerTest {

    private val optimizer = KnapsackCargoOptimizer()

    private val originWarehouse = Warehouse(
        id = "WH-000001",
        name = "Origin Warehouse",
        regionalZone = "North",
        latitude = 31.5,
        longitude = 34.5
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000002",
        name = "Destination Warehouse",
        regionalZone = "South",
        latitude = 31.6,
        longitude = 34.6
    )

    @Test
    fun `returns empty list when packages are empty`() {
        // Given
        val packages = emptyList<Package>()
        val capacityKg = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `selects one package when its weight is within capacity`() {
        // Given
        val logisticsPackage = Package(
            id = "PKG-000001",
            weightByKg = 5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        val packages = listOf(logisticsPackage)
        val capacityKg = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result).containsExactly(logisticsPackage)
    }
}
