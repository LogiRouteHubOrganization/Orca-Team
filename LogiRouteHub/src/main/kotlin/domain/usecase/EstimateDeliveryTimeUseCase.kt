package domain.usecase

import domain.model.Route

class EstimateDeliveryTimeUseCase(
    private val estimateRouteRiskUseCase: EstimateRouteRiskUseCase,
    private val mediumRiskBufferMin: Double,
    private val highRiskBufferMin: Double
) {

    private val minutesPerHour = 60.0
    private val initialTotalTime = 0.0

    operator fun invoke(
        routes: List<Route>,
        averageSpeedKmH: Double
    ): Double {
        if (averageSpeedKmH <= initialTotalTime) {
            return initialTotalTime
        }
        return routes.map { route ->
            val travelTimeMin =
                (route.distanceKm / averageSpeedKmH) * minutesPerHour

            val routeRiskLevel = estimateRouteRiskUseCase(route)

            val riskBufferMin =
                when (routeRiskLevel) {
                    RouteRiskLevel.LOW -> initialTotalTime

                    RouteRiskLevel.MEDIUM -> mediumRiskBufferMin

                    RouteRiskLevel.HIGH -> highRiskBufferMin
                }
            travelTimeMin + route.typicalDelayMin + riskBufferMin
        }.fold(initialTotalTime) { totalTime, routeTime ->
            totalTime + routeTime
        }
    }
}
