package presentation.cli

import domain.service.TelemetryStreamingService
import domain.usecase.TrackActiveFleetUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class FleetTrackingCommand(
    private val telemetryService: TelemetryStreamingService,
    private val trackActiveFleetUseCase: TrackActiveFleetUseCase
) {

    private var trackingJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (trackingJob?.isActive == true) {
            println("Fleet tracking is already running.")
            return
        }

        val job = SupervisorJob(scope.coroutineContext[Job])
        trackingJob = job

        val trackingScope = CoroutineScope(
            scope.coroutineContext + job
        )

        println("Fleet tracking started.")

        trackingScope.launch {
            try {
                telemetryService.streamTelemetry()
                    .collect { telemetry ->
                        println()
                        println("Vehicle: ${telemetry.vehicleId}")
                        println("Latitude: ${telemetry.latitude}")
                        println("Longitude: ${telemetry.longitude}")
                        println("Speed: ${telemetry.speedKmh} km/h")
                        println("Fuel: ${telemetry.fuelPercentage}%")
                        println("Time: ${telemetry.timestamp}")
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                println("Telemetry error: ${error.message}")
            }
        }

        trackingScope.launch {
            try {
                trackActiveFleetUseCase()
                    .collect { notification ->
                        println()
                        println("WARNING: Fleet delay detected!")
                        println("Vehicle: ${notification.vehicleId}")
                        println("Speed: ${notification.speedKmh} km/h")
                        println("Detected at: ${notification.detectedAt}")
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                println("Tracking error: ${error.message}")
            }
        }
    }

    suspend fun stop() {
        trackingJob?.cancelAndJoin()
        trackingJob = null
    }
}