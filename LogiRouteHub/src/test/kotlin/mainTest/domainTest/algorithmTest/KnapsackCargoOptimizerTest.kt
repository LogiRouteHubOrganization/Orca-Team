package mainTest.domainTest.algorithmTest

import com.google.common.truth.Truth.assertThat
import domain.algorithm.knapsack.KnapsackCargoOptimizer
import domain.model.Package
import org.junit.jupiter.api.Test

class KnapsackCargoOptimizerTest {

    private val optimizer = KnapsackCargoOptimizer()

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
}
