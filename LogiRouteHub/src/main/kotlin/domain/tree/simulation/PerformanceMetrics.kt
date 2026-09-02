package domain.tree.simulation

data class PerformanceMetrics(
    val treeType: TreeType,
    val searchSteps: Int,
    val executionTimeNanos: Long
)
