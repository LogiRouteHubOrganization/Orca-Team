package domain.algorithm.tree.simulation

data class SimulationResult(
    val unbalancedTreeMetrics: PerformanceMetrics,
    val balancedTreeMetrics: PerformanceMetrics
)
