package domain.usecase

import domain.model.Route
import domain.model.request.EstimateDeliveryTimeRequest
import domain.model.request.RouteRiskLevel

class EstimateDeliveryTimeUseCase(
    private val estimateRouteRiskUseCase: EstimateRouteRiskUseCase
) {

    operator fun invoke(
        request: EstimateDeliveryTimeRequest
    ): Double {

        if (
            hasInvalidAverageSpeed(
                averageSpeedKmH = request.averageSpeedKmH,
                zeroTime = request.zeroTime
            )
        ) {
            return request.zeroTime
        }

        val routeDeliveryTimes = request.routes.map { route ->
            calculateRouteDeliveryTime(
                route = route,
                request = request
            )
        }

        return calculateTotalDeliveryTime(routeDeliveryTimes)
    }

    private fun hasInvalidAverageSpeed(
        averageSpeedKmH: Double,
        zeroTime: Double
    ): Boolean {
        return averageSpeedKmH <= zeroTime
    }

    private fun calculateRouteDeliveryTime(
        route: Route,
        request: EstimateDeliveryTimeRequest
    ): Double {

        val travelTimeMin = calculateTravelTime(
            route = route,
            averageSpeedKmH = request.averageSpeedKmH,
            minutesPerHour = request.minutesPerHour
        )

        val riskBufferMin = getRiskBuffer(
            route = route,
            request = request
        )

        return addRouteTimeComponents(
            travelTimeMin = travelTimeMin,
            typicalDelayMin = route.typicalDelayMin.toDouble(),
            riskBufferMin = riskBufferMin
        )
    }

    private fun calculateTravelTime(
        route: Route,
        averageSpeedKmH: Double,
        minutesPerHour: Double
    ): Double {

        val travelTimeHours = route.distanceKm / averageSpeedKmH

        return travelTimeHours * minutesPerHour
    }

    private fun getRiskBuffer(
        route: Route,
        request: EstimateDeliveryTimeRequest
    ): Double {

        val routeRiskLevel = estimateRouteRiskUseCase(route)

        return when (routeRiskLevel) {
            RouteRiskLevel.LOW -> request.zeroTime

            RouteRiskLevel.MEDIUM -> request.mediumRiskBufferMinutes

            RouteRiskLevel.HIGH -> request.highRiskBufferMinutes
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
