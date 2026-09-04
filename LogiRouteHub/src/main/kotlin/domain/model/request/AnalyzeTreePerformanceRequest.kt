package domain.model.request

data class AnalyzeTreePerformanceRequest(
    val numberOfTrackingIds: Int,
    val nanoTimeProvider: () -> Long
)
