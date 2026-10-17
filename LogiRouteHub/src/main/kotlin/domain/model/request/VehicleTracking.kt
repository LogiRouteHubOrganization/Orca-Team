package domain.model.request

import java.time.Instant

data class VehicleTracking(
    val startedAt: Instant,
    var lastTimestamp: Instant,
    var notificationSent: Boolean = false
)

