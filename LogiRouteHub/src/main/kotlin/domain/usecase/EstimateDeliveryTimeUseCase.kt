package domain.usecase

import domain.model.Route

class EstimateDeliveryTimeUseCase {

    private val minutesPerHour = 60.0
    private val initialTotalTime = 0.0

    operator fun invoke(
        routes: List<Route>,
        averageSpeedKmH: Double
    ): Double {
        if (averageSpeedKmH <= initialTotalTime) return initialTotalTime

        return routes.map { route ->
            val travelTimeMin =
                (route.distanceKm / averageSpeedKmH) * minutesPerHour

            travelTimeMin + route.typicalDelayMin
        }
            .fold(initialTotalTime) { totalTime, routeTime ->
                totalTime + routeTime
            }
    }
}
