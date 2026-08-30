package domain.usecase

import domain.model.Route

enum class RouteRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

class EstimateRouteRiskUseCase(
    private val highDistanceThresholdKm: Double,
    private val highDelayThresholdMin: Int
) {

    private val mediumRiskScoreThreshold = 1.0
    private val highRiskScoreThreshold = 2.0

    operator fun invoke(route: Route): RouteRiskLevel {
        val distanceRiskScore =
            route.distanceKm / highDistanceThresholdKm

        val delayRiskScore =
            route.typicalDelayMin.toDouble() / highDelayThresholdMin

        val totalRiskScore = distanceRiskScore + delayRiskScore

        return when {
            totalRiskScore >= highRiskScoreThreshold ->
                RouteRiskLevel.HIGH

            totalRiskScore >= mediumRiskScoreThreshold ->
                RouteRiskLevel.MEDIUM

            else -> RouteRiskLevel.LOW
        }
    }
}