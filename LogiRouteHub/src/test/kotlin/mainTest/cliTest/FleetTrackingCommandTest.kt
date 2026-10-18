package mainTest.cliTest

import domain.model.request.VehicleTelemetry
import domain.model.request.FleetDelayNotification
import domain.service.TelemetryStreamingService
import domain.usecase.TrackActiveFleetUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import presentation.cli.ConsoleIO
import presentation.cli.FleetTrackingCommand
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FleetTrackingCommandTest {

    private lateinit var telemetryService: TelemetryStreamingService
    private lateinit var trackActiveFleetUseCase: TrackActiveFleetUseCase
    private lateinit var console: ConsoleIO
    private lateinit var command: FleetTrackingCommand

    private lateinit var telemetryFlow: MutableSharedFlow<VehicleTelemetry>
    private lateinit var notificationFlow: MutableSharedFlow<FleetDelayNotification>

    @BeforeEach
    fun setUp() {
        telemetryService = mockk()
        trackActiveFleetUseCase = mockk()
        console = mockk(relaxed = true)

        telemetryFlow = MutableSharedFlow()
        notificationFlow = MutableSharedFlow()

        every {
            telemetryService.streamTelemetry()
        } returns telemetryFlow

        every {
            trackActiveFleetUseCase()
        } returns notificationFlow

        command = FleetTrackingCommand(
            telemetryService = telemetryService,
            trackActiveFleetUseCase = trackActiveFleetUseCase,
            console = console
        )
    }

    @Test
    fun `should start telemetry and fleet delay tracking`() = runTest {
        // Given
        // The command and both flows are configured in setUp.

        // When
        command.start(backgroundScope)
        runCurrent()

        // Then
        verify(exactly = 1) {
            telemetryService.streamTelemetry()
        }
        verify(exactly = 1) {
            trackActiveFleetUseCase()
        }
        verify {
            console.println("Fleet tracking started.")
        }

        // Cleanup
        command.stop()
    }

    @Test
    fun `should display telemetry when telemetry is emitted`() = runTest {
        // Given
        val telemetry = VehicleTelemetry(
            vehicleId = "TRK-0001",
            latitude = 31.5,
            longitude = 34.5,
            speedKmh = 60.0,
            fuelPercentage = 75.0,
            timestamp = Instant.parse("2026-10-17T08:00:00Z")
        )

        command.start(backgroundScope)
        runCurrent()

        // When
        telemetryFlow.emit(telemetry)
        runCurrent()

        // Then
        verify { console.println("Vehicle: TRK-0001") }
        verify { console.println("Latitude: 31.5") }
        verify { console.println("Longitude: 34.5") }
        verify { console.println("Speed: 60.0 km/h") }
        verify { console.println("Fuel: 75.0%") }

        command.stop()
    }

    @Test
    fun `should display fleet delay notification when emitted`() = runTest {
        // Given
        val notification = FleetDelayNotification(
            vehicleId = "TRK-0001",
            speedKmh = 2.0,
            detectedAt = Instant.parse("2026-10-17T08:05:00Z")
        )

        command.start(backgroundScope)
        runCurrent()

        // When
        notificationFlow.emit(notification)
        runCurrent()

        // Then
        verify {
            console.println("WARNING: Fleet delay detected!")
        }
        verify { console.println("Vehicle: TRK-0001") }
        verify { console.println("Speed: 2.0 km/h") }

        command.stop()
    }

    @Test
    fun `should not start tracking more than once`() = runTest {
        // Given
        command.start(backgroundScope)
        runCurrent()

        // When
        command.start(backgroundScope)
        runCurrent()

        // Then
        verify(exactly = 1) {
            telemetryService.streamTelemetry()
        }
        verify(exactly = 1) {
            trackActiveFleetUseCase()
        }
        verify {
            console.println("Fleet tracking is already running.")
        }

        command.stop()
    }

    @Test
    fun `should stop collecting telemetry and notifications`() = runTest {
        // Given
        command.start(backgroundScope)
        runCurrent()

        // When
        command.stop()

        telemetryFlow.emit(
            VehicleTelemetry(
                vehicleId = "TRK-0002",
                latitude = 31.5,
                longitude = 34.5,
                speedKmh = 40.0,
                fuelPercentage = 80.0,
                timestamp = Instant.parse("2026-10-17T08:10:00Z")
            )
        )

        notificationFlow.emit(
            FleetDelayNotification(
                vehicleId = "TRK-0002",
                speedKmh = 2.0,
                detectedAt = Instant.parse("2026-10-17T08:15:00Z")
            )
        )

        runCurrent()

        // Then
        verify(exactly = 0) {
            console.println("Vehicle: TRK-0002")
        }
        verify(exactly = 0) {
            console.println("WARNING: Fleet delay detected!")
        }
    }
}
