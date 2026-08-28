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

    operator fun invoke(route: Route): RouteRiskLevel {
        val hasHighDistance = route.distanceKm >= highDistanceThresholdKm

        val hasHighDelay = route.typicalDelayMin >= highDelayThresholdMin

        return when {
            hasHighDistance && hasHighDelay -> RouteRiskLevel.HIGH

            hasHighDistance || hasHighDelay -> RouteRiskLevel.MEDIUM

            else -> RouteRiskLevel.LOW
        }
    }
}
