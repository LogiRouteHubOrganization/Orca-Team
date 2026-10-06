package mainTest.domainTest.algorithmTest

import com.google.common.truth.Truth.assertThat
import domain.algorithm.knapsack.TwoDimensionalKnapsackCargoOptimizer
import domain.model.Package
import domain.model.Warehouse
import org.junit.jupiter.api.Test

class TwoDimensionalKnapsackCargoOptimizerTest {

    private val optimizer = TwoDimensionalKnapsackCargoOptimizer()

    private val originWarehouse = Warehouse(
        id = "WH-000001",
        name = "Origin Warehouse",
        regionalZone = "North",
        latitude = 31.5,
        longitude = 34.5
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-000004",
        name = "Destination Warehouse",
        regionalZone = "South",
        latitude = 25.5,
        longitude = 34.6
    )

    @Test
    fun `should return empty list when packages are empty`() {
        // Given
        val packages = emptyList<Package>()
        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should select package when its weight and volume are within capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000001",
            weightByKg = 5.0,
            volumeM3 = 4.0,
            priority = "URGENT"
        )

        val packages = listOf(logisticsPackage)
        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).containsExactly(logisticsPackage)
    }

    @Test
    fun `should not select package when its weight exceeds capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000002",
            weightByKg = 15.0,
            volumeM3 = 2.0,
            priority = "URGENT"
        )

        val packages = listOf(logisticsPackage)
        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should not select package when its volume exceeds capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000003",
            weightByKg = 5.0,
            volumeM3 = 15.0,
            priority = "URGENT"
        )

        val packages = listOf(logisticsPackage)
        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should select package when its weight and volume equal capacity`() {
        // Given
        val logisticsPackage = createPackage(
            id = "PKG-000004",
            weightByKg = 15.0,
            volumeM3 = 15.0,
            priority = "STANDARD"
        )

        val packages = listOf(logisticsPackage)
        val capacityKg = 15.0
        val capacityVolumeM3 = 15.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).containsExactly(logisticsPackage)
    }

    @Test
    fun `should not select package combination when total volume exceeds capacity`() {
        // Given
        val firstPackage = createPackage(
            id = "PKG-000005",
            weightByKg = 4.0,
            volumeM3 = 7.0,
            priority = "URGENT"
        )

        val secondPackage = createPackage(
            id = "PKG-000006",
            weightByKg = 5.0,
            volumeM3 = 9.0,
            priority = "STANDARD"
        )

        val packages = listOf(firstPackage, secondPackage)

        val capacityKg = 10.0
        val capacityVolumeM3 = 14.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result.sumOf { it.weightByKg })
            .isAtMost(capacityKg)

        assertThat(result.sumOf { it.volumeM3 ?: 0.0 })
            .isAtMost(capacityVolumeM3)
    }

    @Test
    fun `should respect both weight and volume constraints`() {
        // Given
        val firstPackage = createPackage(
            id = "PKG-000007",
            weightByKg = 6.0,
            volumeM3 = 4.0,
            priority = "URGENT"
        )

        val secondPackage = createPackage(
            id = "PKG-000008",
            weightByKg = 4.0,
            volumeM3 = 3.0,
            priority = "STANDARD"
        )

        val thirdPackage = createPackage(
            id = "PKG-000009",
            weightByKg = 5.0,
            volumeM3 = 8.0,
            priority = "LOW"
        )

        val packages = listOf(
            firstPackage,
            secondPackage,
            thirdPackage
        )

        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result.sumOf { it.weightByKg })
            .isAtMost(capacityKg)

        assertThat(result.sumOf { it.volumeM3 ?: 0.0 })
            .isAtMost(capacityVolumeM3)
    }

    @Test
    fun `should handle bulky low-weight package within weight but limited by volume`() {
        // Given
        val bulkyPackage = createPackage(
            id = "PKG-000010",
            weightByKg = 2.0,
            volumeM3 = 9.0,
            priority = "URGENT"
        )

        val packages = listOf(bulkyPackage)
        val capacityKg = 10.0
        val capacityVolumeM3 = 8.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    fun `should handle dense high-weight package when volume is within capacity`() {
        // Given
        val densePackage = createPackage(
            id = "PKG-000011",
            weightByKg = 8.0,
            volumeM3 = 2.0,
            priority = "URGENT"
        )

        val packages = listOf(densePackage)
        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).containsExactly(densePackage)
    }

    @Test
    fun `should change selected packages when volume constraint is introduced`() {
        // Given
        val bulkyPackage = createPackage(
            id = "PKG-000012",
            weightByKg = 2.0,
            volumeM3 = 9.0,
            priority = "STANDARD"
        )

        val densePackage = createPackage(
            id = "PKG-000013",
            weightByKg = 8.0,
            volumeM3 = 2.0,
            priority = "URGENT"
        )

        val compactPackage = createPackage(
            id = "PKG-000014",
            weightByKg = 2.0,
            volumeM3 = 1.0,
            priority = "URGENT"
        )

        val packages = listOf(
            bulkyPackage,
            densePackage,
            compactPackage
        )

        val capacityKg = 10.0
        val capacityVolumeM3 = 10.0

        // When
        val result = optimizer.optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )

        // Then
        assertThat(result).containsExactly(
            densePackage,
            compactPackage
        )

        assertThat(result.sumOf { it.weightByKg })
            .isAtMost(capacityKg)

        assertThat(result.sumOf { it.volumeM3 ?: 0.0 })
            .isAtMost(capacityVolumeM3)
    }

    private fun createPackage(
        id: String,
        weightByKg: Double,
        volumeM3: Double,
        priority: String
    ): Package {
        return Package(
            id = id,
            weightByKg = weightByKg,
            priority = priority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse,
            volumeM3 = volumeM3
        )
    }
}
