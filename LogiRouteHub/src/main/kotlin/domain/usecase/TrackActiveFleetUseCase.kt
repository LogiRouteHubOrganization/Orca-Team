
package domain.usecase

import domain.model.VehicleTelemetry
import domain.model.request.FleetDelayNotification
import domain.service.TelemetryStreamingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import java.time.Duration
import java.time.Instant

class TrackActiveFleetUseCase(
    private val telemetryStreamingService: TelemetryStreamingService,
    private val lowSpeedThresholdKmh: Double = 5.0,
    private val delayThreshold: Duration = Duration.ofMinutes(5)
) {

    init {
        require(
            lowSpeedThresholdKmh.isFinite() &&
                    lowSpeedThresholdKmh >= 0.0
        ) {
            "Low speed threshold must be finite and non-negative"
        }

        require(
            !delayThreshold.isNegative &&
                    !delayThreshold.isZero
        ) {
            "Delay threshold must be positive"
        }
    }

    operator fun invoke(): Flow<FleetDelayNotification> = flow {
        // One state per collection, shared by all telemetry updates.
        val state = DelayTrackingState()

        telemetryStreamingService.streamTelemetry()
            .transform { telemetry ->
                processTelemetry(telemetry, state)?.let { notification ->
                    emit(notification)
                }
            }
            .collect { notification ->
                emit(notification)
            }
    }

    private fun processTelemetry(
        telemetry: VehicleTelemetry,
        state: DelayTrackingState
    ): FleetDelayNotification? {
        if (!isMovingBelowThreshold(telemetry)) {
            resetTracking(telemetry.vehicleId, state)
            return null
        }

        return detectDelay(telemetry, state)
    }

    private fun isMovingBelowThreshold(
        telemetry: VehicleTelemetry
    ): Boolean =
        telemetry.speedKmh < lowSpeedThresholdKmh

    private fun detectDelay(
        telemetry: VehicleTelemetry,
        state: DelayTrackingState
    ): FleetDelayNotification? {
        val tracking = getOrStartTracking(telemetry, state)

        // A timestamp moving backwards starts a new tracking period.
        if (tracking == null) {
            return null
        }

        tracking.lastTimestamp = telemetry.timestamp

        val duration = Duration.between(
            tracking.startedAt,
            telemetry.timestamp
        )

        if (
            duration < delayThreshold ||
            tracking.notificationSent
        ) {
            return null
        }

        tracking.notificationSent = true

        return telemetry.toDelayNotification()
    }

    private fun getOrStartTracking(
        telemetry: VehicleTelemetry,
        state: DelayTrackingState
    ): VehicleTracking? {
        val vehicleId = telemetry.vehicleId
        val existing = state.trackingByVehicle[vehicleId]

        if (existing == null) {
            state.trackingByVehicle[vehicleId] =
                VehicleTracking(
                    startedAt = telemetry.timestamp,
                    lastTimestamp = telemetry.timestamp
                )

            return null
        }

        if (telemetry.timestamp.isBefore(existing.lastTimestamp)) {
            state.trackingByVehicle[vehicleId] =
                VehicleTracking(
                    startedAt = telemetry.timestamp,
                    lastTimestamp = telemetry.timestamp
                )

            return null
        }

        return existing
    }

    private fun resetTracking(
        vehicleId: String,
        state: DelayTrackingState
    ) {
        state.trackingByVehicle.remove(vehicleId)
    }

    private fun VehicleTelemetry.toDelayNotification() =
        FleetDelayNotification(
            vehicleId = vehicleId,
            speedKmh = speedKmh,
            detectedAt = timestamp
        )

    private class DelayTrackingState {
        val trackingByVehicle = mutableMapOf<String, VehicleTracking>()
    }

    private data class VehicleTracking(
        val startedAt: Instant,
        var lastTimestamp: Instant,
        var notificationSent: Boolean = false
    )
}
