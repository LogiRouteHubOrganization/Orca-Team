package domain.performance

data class SimulationResult(
    val unbalancedTreeMetrics: PerformanceMetrics,
    val balancedTreeMetrics: PerformanceMetrics
)
