package domain.algorithm.knapsack

import domain.model.Package
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse

class CargoOptimizerTemplateTest {

    @Test
    fun `should not perform optimization when packages are empty`() {
        val optimizer = TestCargoOptimizer()

        optimizer.optimize(
            packages = emptyList(),
            capacityKg = 10.0
        )

        assertFalse(optimizer.wasOptimizationPerformed)
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
