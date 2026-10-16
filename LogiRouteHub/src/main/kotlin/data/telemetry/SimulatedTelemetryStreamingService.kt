package data.telemetry

import domain.model.VehicleTelemetry
import domain.service.TelemetryStreamingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import kotlin.random.Random

class SimulatedTelemetryStreamingService(
    private val vehicleIds: List<String> = listOf(
        "TRK-000001",
        "TRK-000002",
        "TRK-000003"
    ),
    private val intervalMillis: Long = 1_000L,
    private val telemetryFactory: (String, Long) -> VehicleTelemetry =
        ::generateTelemetry
) : TelemetryStreamingService {

    init {
        require(vehicleIds.isNotEmpty()) {
            "At least one vehicle is required"
        }

        require(intervalMillis > 0) {
            "Interval must be positive"
        }
    }

    override fun streamTelemetry(): Flow<VehicleTelemetry> = flow {
        var sequence = 0L

        while (true) {
            vehicleIds.forEach { vehicleId ->
                emit(telemetryFactory(vehicleId, sequence))
            }

            sequence++
            delay(intervalMillis)
        }
    }

    private companion object {
        fun generateTelemetry(
            vehicleId: String,
            sequence: Long
        ): VehicleTelemetry {
            val movement = (sequence % 100) * 0.0001

            return VehicleTelemetry(
                vehicleId = vehicleId,
                latitude = 31.5 + movement +
                        Random.Default.nextDouble(-0.02, 0.02),
                longitude = 34.45 + movement +
                        Random.Default.nextDouble(-0.02, 0.02),
                speedKmh = Random.Default.nextDouble(0.0, 120.0),
                fuelPercentage = (100.0 - sequence * 0.5)
                    .coerceAtLeast(0.0),
                timestamp = Instant.now()
            )
        }
    }
}