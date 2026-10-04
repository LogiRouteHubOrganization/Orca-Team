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
    @Test
    fun `should not select packages whose total weight exceeds capacity`() {
        // Given
        val firstPackage = Package(
            id = "PKG-000001",
            weightByKg = 6.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        val secondPackage = Package(
            id = "PKG-000002",
            weightByKg = 5.0,
            priority = "STANDARD",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
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
        val logisticsPackage = Package(
            id = "PKG-000003",
            weightByKg = 15.0,
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
        assertThat(result).isEmpty()
    }
    @Test
    fun `should select package when its weight equals capacity`() {
        // Given
        val logisticsPackage = Package(
            id = "PKG-000004",
            weightByKg = 10.0,
            priority = "STANDARD",
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
    @Test
    fun `should return empty list when capacity is zero`() {
        // Given
        val logisticsPackage = Package(
            id = "PKG-000005",
            weightByKg = 5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
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
        val firstPackage = Package(
            id = "PKG-000006",
            weightByKg = 6.0,
            priority = "STANDARD",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        val secondPackage = Package(
            id = "PKG-000007",
            weightByKg = 5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        val thirdPackage = Package(
            id = "PKG-000008",
            weightByKg = 5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
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
        val firstPackage = Package(
            id = "PKG-000009",
            weightByKg = 6.0,
            priority = "STANDARD",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        val secondPackage = Package(
            id = "PKG-000010",
            weightByKg = 5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        val thirdPackage = Package(
            id = "PKG-000011",
            weightByKg = 5.0,
            priority = "URGENT",
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
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
}
