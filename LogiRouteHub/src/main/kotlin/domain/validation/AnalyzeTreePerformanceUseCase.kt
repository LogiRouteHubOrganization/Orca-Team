package domain.validation

import domain.tree.simulation.*

class AnalyzeTreePerformanceUseCase {

    operator fun invoke(): SimulationResult {

        val numberOfTrackingIds = 1000
        val generator = SequentialTrackingIdGenerator()
        val trackingIds = generator.generatePackageTrackingIds(numberOfTrackingIds)
        val targetTrackingId = trackingIds.last()
        val unbalancedBST = UnbalancedBST()

        trackingIds.forEach { trackingId ->
            unbalancedBST.insert(trackingId)
        }

        val unbalancedStartTime = System.nanoTime()
        val unbalancedSteps = unbalancedBST.searchSteps(targetTrackingId)
        val unbalancedExecutionTime = System.nanoTime() - unbalancedStartTime
        val balancedBST = BalancedBST()
        balancedBST.build(trackingIds)
        val balancedStartTime = System.nanoTime()
        val balancedSteps = balancedBST.searchSteps(targetTrackingId)
        val balancedExecutionTime = System.nanoTime() - balancedStartTime
        val result = SimulationResult(
            unbalancedTreeMetrics = PerformanceMetrics(
                treeType = "Unbalanced BST",
                searchSteps = unbalancedSteps,
                executionTimeNanos = unbalancedExecutionTime
            ),
            balancedTreeMetrics = PerformanceMetrics(
                treeType = "Balanced BST",
                searchSteps = balancedSteps,
                executionTimeNanos = balancedExecutionTime
            )
        )
        printResult(result)
        return result
    }

    private fun printResult(result: SimulationResult) {

        println("===== TREE PERFORMANCE ANALYSIS =====")

        println("\n${result.unbalancedTreeMetrics.treeType}")
        println("Steps: ${result.unbalancedTreeMetrics.searchSteps}")
        println(
            "Time: ${result.unbalancedTreeMetrics.executionTimeNanos} ns"
        )

        println("\n${result.balancedTreeMetrics.treeType}")
        println("Steps: ${result.balancedTreeMetrics.searchSteps}")
        println(
            "Time: ${result.balancedTreeMetrics.executionTimeNanos} ns"
        )
    }
}