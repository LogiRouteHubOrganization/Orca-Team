package domain.performance

data class PerformanceMetrics(
    val treeType: String,
    val searchSteps: Int,
    val executionTimeNanos: Long
)
