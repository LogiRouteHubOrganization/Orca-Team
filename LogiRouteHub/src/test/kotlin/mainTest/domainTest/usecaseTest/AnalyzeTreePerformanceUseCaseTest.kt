package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.request.AnalyzeTreePerformanceRequest
import domain.algorithm.tree.simulation.BalancedBST
import domain.algorithm.tree.simulation.SequentialTrackingIdGenerator
import domain.algorithm.tree.simulation.TreeType
import domain.algorithm.tree.simulation.UnbalancedBST
import domain.usecase.AnalyzeTreePerformanceUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class AnalyzeTreePerformanceUseCaseTest {

    @Test
    fun `should analyze performance of both trees`() {
        // Given
        val generator = mockk<SequentialTrackingIdGenerator>()
        val unbalancedBST = mockk<UnbalancedBST>(relaxed = true)
        val balancedBST = mockk<BalancedBST>(relaxed = true)

        setupTreePerformanceStubs(generator, unbalancedBST, balancedBST)

        var time = 0L
        val request = AnalyzeTreePerformanceRequest(
            numberOfTrackingIds = 3,
            nanoTimeProvider = {
                time += 100L
                time
            }
        )

        val useCase = AnalyzeTreePerformanceUseCase(
            generator = generator,
            unbalancedBST = unbalancedBST,
            balancedBST = balancedBST
        )

        // When
        val result = useCase(request)

        // Then
        assertThat(result.unbalancedTreeMetrics.treeType).isEqualTo(TreeType.UNBALANCED_BST)
        assertThat(result.unbalancedTreeMetrics.searchSteps).isEqualTo(5)
        assertThat(result.balancedTreeMetrics.treeType).isEqualTo(TreeType.BALANCED_BST)
        assertThat(result.balancedTreeMetrics.searchSteps).isEqualTo(2)

        verify { generator.generatePackageTrackingIds(3) }
        verify { unbalancedBST.searchSteps("PKG-003") }
        verify { balancedBST.searchSteps("PKG-003") }
    }

    private fun setupTreePerformanceStubs(
        generator: SequentialTrackingIdGenerator,
        unbalancedBST: UnbalancedBST,
        balancedBST: BalancedBST
    ) {
        val trackingIds = listOf("PKG-001", "PKG-002", "PKG-003")

        every { generator.generatePackageTrackingIds(3) } returns trackingIds
        every { unbalancedBST.searchSteps("PKG-003") } returns 5
        every { balancedBST.searchSteps("PKG-003") } returns 2
    }
}
