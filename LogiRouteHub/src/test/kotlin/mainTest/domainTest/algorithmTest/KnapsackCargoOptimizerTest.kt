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
    fun `should return empty list when packages are empty`() {
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
    fun `should select one package when its weight is within capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000001",
            weightByKg = 5.0,
            priority = "URGENT"
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

    @Test
    fun `should not select packages whose total weight exceeds capacity`() {
        // Given
        val firstPackage = createPackage(
            id = "PKG-000001",
            weightByKg = 6.0,
            priority = "URGENT"
        )

        val secondPackage = createPackage(
            id = "PKG-000002",
            weightByKg = 5.0,
            priority = "STANDARD"
        )

        val packages = listOf(
            firstPackage,
            secondPackage
        )
        val capacityKg = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result.sumOf { it.weightByKg }).isAtMost(capacityKg)
    }

    @Test
    fun `should not select package when its weight exceeds capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000003",
            weightByKg = 15.0,
            priority = "URGENT"
        )

        val packages = listOf(logisticsPackage)
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
    fun `should select package when its weight equals capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000004",
            weightByKg = 10.0,
            priority = "STANDARD"
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

    @Test
    fun `should return empty list when capacity is zero`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000005",
            weightByKg = 5.0,
            priority = "URGENT"
        )

        val packages = listOf(logisticsPackage)
        val capacityKg = 0.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should select optimal package combination to maximize total priority within capacity`() {
        // Given
        val firstPackage = createPackage(
            id = "PKG-000006",
            weightByKg = 6.0,
            priority = "STANDARD"
        )

        val secondPackage = createPackage(
            id = "PKG-000007",
            weightByKg = 5.0,
            priority = "URGENT"
        )

        val thirdPackage = createPackage(
            id = "PKG-000008",
            weightByKg = 5.0,
            priority = "URGENT"
        )

        val packages = listOf(
            firstPackage,
            secondPackage,
            thirdPackage
        )

        val capacityKg = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result).containsExactly(
            secondPackage,
            thirdPackage
        )
    }

    @Test
    fun `should return the exact packages selected by optimal solution`() {
        // Given
        val firstPackage = createPackage(
            id = "PKG-000009",
            weightByKg = 6.0,
            priority = "STANDARD"
        )

        val secondPackage = createPackage(
            id = "PKG-000010",
            weightByKg = 5.0,
            priority = "URGENT"
        )

        val thirdPackage = createPackage(
            id = "PKG-000011",
            weightByKg = 5.0,
            priority = "URGENT"
        )

        val packages = listOf(
            firstPackage,
            secondPackage,
            thirdPackage
        )

        val capacityKg = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result).containsExactly(
            secondPackage,
            thirdPackage
        )
    }

    @Test
    fun `should return empty list when all packages exceed capacity`() {
        // Given
        val firstPackage = createPackage(
            id = "PKG-000012",
            weightByKg = 15.0,
            priority = "URGENT"
        )

        val secondPackage = createPackage(
            id = "PKG-000013",
            weightByKg = 20.0,
            priority = "STANDARD"
        )

        val thirdPackage = createPackage(
            id = "PKG-000014",
            weightByKg = 12.0,
            priority = "LOW"
        )

        val packages = listOf(
            firstPackage,
            secondPackage,
            thirdPackage
        )

        val capacityKg = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg
        )

        // Then
        assertThat(result).isEmpty()
    }

    private fun createPackage(
        id: String,
        weightByKg: Double,
        priority: String
    ): Package {
        return Package(
            id = id,
            weightByKg = weightByKg,
            priority = priority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}