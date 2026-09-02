package domain.usecase

import domain.model.Route
import domain.model.request.RouteRiskConfig
import domain.model.request.RouteRiskLevel

class EstimateRouteRiskUseCase(
    private val riskConfig: RouteRiskConfig
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
        return route.distanceKm / riskConfig.highDistanceThresholdKm
    }

    private fun calculateDelayRiskScore(
        route: Route
    ): Double {
        return route.typicalDelayMin.toDouble() /
                riskConfig.highDelayThresholdMin
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
            totalRiskScore >= riskConfig.highRiskScoreThreshold ->
                RouteRiskLevel.HIGH

            totalRiskScore >= riskConfig.mediumRiskScoreThreshold ->
                RouteRiskLevel.MEDIUM

            else ->
                RouteRiskLevel.LOW
        }
    }
}
