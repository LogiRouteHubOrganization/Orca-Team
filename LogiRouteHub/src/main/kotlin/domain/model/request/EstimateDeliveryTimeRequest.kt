package domain.model.request

import domain.model.Route

data class EstimateDeliveryTimeRequest(
    val routes: List<Route>,
    val averageSpeedKmH: Double,
    val mediumRiskBufferMinutes: Double,
    val highRiskBufferMinutes: Double,
    val minutesPerHour: Double = 60.0,
    val zeroTime: Double = 0.0
)
