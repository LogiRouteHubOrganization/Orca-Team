package domain.model.request

data class RouteRiskConfig(
    val highDistanceThresholdKm: Double,
    val highDelayThresholdMin: Int,
    val mediumRiskScoreThreshold: Double = 1.0,
    val highRiskScoreThreshold: Double = 2.0
)
