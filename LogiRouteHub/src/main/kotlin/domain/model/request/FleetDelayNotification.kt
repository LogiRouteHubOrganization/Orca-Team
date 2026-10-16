package domain.model.request

import java.time.Instant

data class FleetDelayNotification(
    val vehicleId: String,
    val speedKmh: Double,
    val detectedAt: Instant
)