package domain.algorithm.knapsack

import domain.model.Package
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class CargoOptimizerTemplateTest {

    @Test
    fun `should not perform optimization when packages are empty`() {
        // Given
        val optimizer = TestCargoOptimizer()
        val packages = emptyList<Package>()

        // When
        optimizer.optimize(
            packages = packages,
            capacityKg = 10.0
        )

        // Then
        assertFalse(optimizer.wasOptimizationPerformed)
    }

    @Test
    fun `should not perform optimization when weight capacity is zero`() {
        // Given
        val optimizer = TestCargoOptimizer()
        val packages = listOf(mockk<Package>())

        // When
        optimizer.optimize(
            packages = packages,
            capacityKg = 0.0
        )

        // Then
        assertFalse(optimizer.wasOptimizationPerformed)
    }

    @Test
    fun `should perform optimization when input is valid`() {
        // Given
        val optimizer = TestCargoOptimizer()
        val packages = listOf(mockk<Package>())

        // When
        optimizer.optimize(
            packages = packages,
            capacityKg = 10.0
        )

        // Then
        assertTrue(optimizer.wasOptimizationPerformed)
    }

    private class TestCargoOptimizer : CargoOptimizerTemplate() {

        var wasOptimizationPerformed = false

        override fun performOptimization(
            packages: List<Package>,
            capacityKg: Double,
            capacityVolumeM3: Double?
        ): List<Package> {
            wasOptimizationPerformed = true
            return packages
        }
    }
}
