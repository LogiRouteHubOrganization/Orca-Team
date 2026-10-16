package domain.usecase

import domain.model.request.AnalyzeTreePerformanceRequest
import domain.algorithm.tree.simulation.SimulationResult
import domain.algorithm.tree.simulation.PerformanceMetrics
import domain.algorithm.tree.simulation.TreeType
import domain.algorithm.tree.simulation.UnbalancedBST
import domain.algorithm.tree.simulation.BalancedBST
import domain.algorithm.tree.simulation.SequentialTrackingIdGenerator

class AnalyzeTreePerformanceUseCase(
    private val generator: SequentialTrackingIdGenerator,
    private val unbalancedBST: UnbalancedBST,
    private val balancedBST: BalancedBST
) {

    operator fun invoke(
        input: AnalyzeTreePerformanceRequest
    ): SimulationResult {

        val trackingIds =
            generator.generatePackageTrackingIds(
                input.numberOfTrackingIds
            )

        val targetTrackingId = trackingIds.last()

        trackingIds.forEach { trackingId ->
            unbalancedBST.insert(trackingId)
        }

        val unbalancedStartTime = input.nanoTimeProvider()

        val unbalancedSteps = unbalancedBST.searchSteps(targetTrackingId)

        val unbalancedExecutionTime = input.nanoTimeProvider() - unbalancedStartTime

        balancedBST.build(trackingIds)

        val balancedStartTime = input.nanoTimeProvider()

        val balancedSteps = balancedBST.searchSteps(targetTrackingId)

        val balancedExecutionTime = input.nanoTimeProvider() - balancedStartTime

        return SimulationResult(
            unbalancedTreeMetrics = PerformanceMetrics(
                treeType = TreeType.UNBALANCED_BST,
                searchSteps = unbalancedSteps,
                executionTimeNanos = unbalancedExecutionTime
            ),
            balancedTreeMetrics = PerformanceMetrics(
                treeType = TreeType.BALANCED_BST,
                searchSteps = balancedSteps,
                executionTimeNanos = balancedExecutionTime
            )
        )
    }
}
