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
    private val trackActiveFleetUseCase: TrackActiveFleetUseCase,
    private val console: ConsoleIO
) {

    private var trackingJob: Job? = null

    fun start(scope: CoroutineScope) {
        if (trackingJob?.isActive == true) {
            console.println("Fleet tracking is already running.")
            return
        }

        val job = SupervisorJob(scope.coroutineContext[Job])
        trackingJob = job

        val trackingScope = CoroutineScope(
            scope.coroutineContext + job
        )

        console.println("Fleet tracking started.")

        trackingScope.launch {
            try {
                telemetryService.streamTelemetry().collect { telemetry ->
                    console.println()
                    console.println("Vehicle: ${telemetry.vehicleId}")
                    console.println("Latitude: ${telemetry.latitude}")
                    console.println("Longitude: ${telemetry.longitude}")
                    console.println("Speed: ${telemetry.speedKmh} km/h")
                    console.println("Fuel: ${telemetry.fuelPercentage}%")
                    console.println("Time: ${telemetry.timestamp}")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                console.println("Telemetry error: ${error.message}")
            }
        }

        trackingScope.launch {
            try {
                trackActiveFleetUseCase().collect { notification ->
                    console.println()
                    console.println("WARNING: Fleet delay detected!")
                    console.println("Vehicle: ${notification.vehicleId}")
                    console.println("Speed: ${notification.speedKmh} km/h")
                    console.println("Detected at: ${notification.detectedAt}")
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                console.println("Tracking error: ${error.message}")
            }
        }
    }

    suspend fun stop() {
        trackingJob?.cancelAndJoin()
        trackingJob = null
    }
}