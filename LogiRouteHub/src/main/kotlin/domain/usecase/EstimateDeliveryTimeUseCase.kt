package domain.usecase

import domain.model.Route
import domain.model.request.RouteRiskLevel
import domain.model.request.EstimateDeliveryTimeRequest

class EstimateDeliveryTimeUseCase(
    private val estimateRouteRiskUseCase: EstimateRouteRiskUseCase,
    private val mediumRiskBufferMinutes: Double,
    private val highRiskBufferMinutes: Double
) {

    private val minutesPerHour = 60.0
    private val zeroTime = 0.0

    operator fun invoke(
        request: EstimateDeliveryTimeRequest
    ): Double {
        if (hasInvalidAverageSpeed(request.averageSpeedKmH)) {
            return zeroTime
        }

        val routeDeliveryTimes = request.routes.map { route ->
            calculateRouteDeliveryTime(
                route = route,
                averageSpeedKmH = request.averageSpeedKmH
            )
        }

        return calculateTotalDeliveryTime(routeDeliveryTimes)
    }

    private fun hasInvalidAverageSpeed(
        averageSpeedKmH: Double
    ): Boolean {
        return averageSpeedKmH <= zeroTime
    }

    private fun calculateRouteDeliveryTime(
        route: Route,
        averageSpeedKmH: Double
    ): Double {
        val travelTimeMin = calculateTravelTime(
            route = route,
            averageSpeedKmH = averageSpeedKmH
        )

        val riskBufferMin = getRiskBuffer(route)

        return addRouteTimeComponents(
            travelTimeMin = travelTimeMin,
            typicalDelayMin = route.typicalDelayMin.toDouble(),
            riskBufferMin = riskBufferMin
        )
    }

    private fun calculateTravelTime(
        route: Route,
        averageSpeedKmH: Double
    ): Double {
        val travelTimeHours = route.distanceKm / averageSpeedKmH

        return travelTimeHours * minutesPerHour
    }

    private fun getRiskBuffer(
        route: Route
    ): Double {

        val routeRiskLevel = estimateRouteRiskUseCase(route)

        return when (routeRiskLevel) {
            RouteRiskLevel.LOW -> zeroTime
            RouteRiskLevel.MEDIUM -> mediumRiskBufferMinutes
            RouteRiskLevel.HIGH -> highRiskBufferMinutes
        }
    }

    private fun addRouteTimeComponents(
        travelTimeMin: Double,
        typicalDelayMin: Double,
        riskBufferMin: Double
    ): Double {
        return travelTimeMin + typicalDelayMin + riskBufferMin
    }

    private fun calculateTotalDeliveryTime(
        routeDeliveryTimes: List<Double>
    ): Double {
        return routeDeliveryTimes.sum()
    }

}
