package domain.request

import domain.model.Route

data class EstimateDeliveryTimeRequest(
    val routes: List<Route>,
    val averageSpeedKmH: Double
)