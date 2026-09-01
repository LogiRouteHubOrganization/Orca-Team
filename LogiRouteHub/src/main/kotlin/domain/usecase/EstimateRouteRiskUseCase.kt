package domain.usecase

import domain.model.Route
import domain.model.request.RouteRiskLevel
import domain.model.request.RouteRiskConfig

class EstimateRouteRiskUseCase(
    private val config: RouteRiskConfig
) {

    operator fun invoke(route: Route): RouteRiskLevel {
        val distanceRiskScore = calculateDistanceRiskScore(route)
        val delayRiskScore = calculateDelayRiskScore(route)
        val totalRiskScore = calculateTotalRiskScore(
            distanceRiskScore = distanceRiskScore,
            delayRiskScore = delayRiskScore
        )

        return determineRiskLevel(totalRiskScore)
    }

    private fun calculateDistanceRiskScore(
        route: Route
    ): Double {
        return route.distanceKm / config.highDistanceThresholdKm
    }

    private fun calculateDelayRiskScore(
        route: Route
    ): Double {
        return route.typicalDelayMin.toDouble() / config.highDelayThresholdMin
    }

    private fun calculateTotalRiskScore(
        distanceRiskScore: Double,
        delayRiskScore: Double
    ): Double {
        return distanceRiskScore + delayRiskScore
    }

    private fun determineRiskLevel(
        totalRiskScore: Double
    ): RouteRiskLevel {
        return when {
            totalRiskScore >= config.highRiskScoreThreshold ->
                RouteRiskLevel.HIGH

            totalRiskScore >= config.mediumRiskScoreThreshold ->
                RouteRiskLevel.MEDIUM

            else -> RouteRiskLevel.LOW
        }
    }
}
