package domain.usecase

import domain.model.Route
import domain.model.request.RouteRiskLevel
import domain.model.request.RouteRiskConfig

class EstimateRouteRiskUseCase(
    private val input: RouteRiskConfig
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
        return route.distanceKm / input.highDistanceThresholdKm
    }

    private fun calculateDelayRiskScore(
        route: Route
    ): Double {
        return route.typicalDelayMin.toDouble() / input.highDelayThresholdMin
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
            totalRiskScore >= input.highRiskScoreThreshold ->
                RouteRiskLevel.HIGH

            totalRiskScore >= input.mediumRiskScoreThreshold ->
                RouteRiskLevel.MEDIUM

            else -> RouteRiskLevel.LOW
        }
    }
}
