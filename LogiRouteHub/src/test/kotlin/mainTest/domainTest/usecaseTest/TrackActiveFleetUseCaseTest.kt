
package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.request.VehicleTelemetry
import domain.model.request.FleetDelayNotification
import domain.service.TelemetryStreamingService
import domain.usecase.TrackActiveFleetUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Duration
import java.time.Instant

class TrackActiveFleetUseCaseTest {

    private val start = Instant.parse("2026-10-16T08:00:00Z")

    @Test
    fun `should not notify after a single low speed update`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start)
        )

        assertThat(useCase().toList()).isEmpty()
    }

    @Test
    fun `should notify when low speed persists for five minutes`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 3.0, start.plusSeconds(240)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(300))
        )

        val notifications = useCase().toList()

        assertThat(notifications).containsExactly(
            FleetDelayNotification(
                vehicleId = "TRK-000001",
                speedKmh = 2.0,
                detectedAt = start.plusSeconds(300)
            )
        )
    }

    @Test
    fun `should not notify before five minutes have passed`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 3.0, start.plusSeconds(299))
        )

        assertThat(useCase().toList()).isEmpty()
    }

    @Test
    fun `should notify only once during continuous low speed`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 2.0, start.plusSeconds(300)),
            telemetry("TRK-000001", 1.0, start.plusSeconds(360)),
            telemetry("TRK-000001", 3.0, start.plusSeconds(420))
        )

        assertThat(useCase().toList()).hasSize(1)
    }

    @Test
    fun `should reset tracking when speed recovers`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 3.0, start.plusSeconds(240)),
            telemetry("TRK-000001", 5.0, start.plusSeconds(270)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(300)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(599))
        )

        assertThat(useCase().toList()).isEmpty()
    }

    @Test
    fun `should notify after five minutes following speed recovery`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 5.0, start.plusSeconds(240)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(300)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(600))
        )

        val notification = useCase().toList().single()

        assertThat(notification.detectedAt)
            .isEqualTo(start.plusSeconds(600))
    }

    @Test
    fun `should notify again after recovery and a new delay`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 2.0, start.plusSeconds(300)),
            telemetry("TRK-000001", 5.0, start.plusSeconds(360)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(420)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(720))
        )

        val notifications = useCase().toList()

        assertThat(notifications.map { it.detectedAt })
            .containsExactly(
                start.plusSeconds(300),
                start.plusSeconds(720)
            )
            .inOrder()
    }

    @Test
    fun `should track each vehicle independently`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000002", 3.0, start.plusSeconds(120)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(300)),
            telemetry("TRK-000002", 3.0, start.plusSeconds(420))
        )

        val notifications = useCase().toList()

        assertThat(notifications.map { it.vehicleId })
            .containsExactly("TRK-000001", "TRK-000002")
            .inOrder()
    }

    @Test
    fun `should restart tracking when timestamp moves backwards`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 2.0, start.plusSeconds(240)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(60)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(359))
        )

        assertThat(useCase().toList()).isEmpty()
    }

    @Test
    fun `should notify after five minutes from reset timestamp`() = runTest {
        val useCase = createUseCase(
            telemetry("TRK-000001", 2.0, start),
            telemetry("TRK-000001", 2.0, start.plusSeconds(240)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(60)),
            telemetry("TRK-000001", 2.0, start.plusSeconds(360))
        )

        val notification = useCase().toList().single()

        assertThat(notification.detectedAt)
            .isEqualTo(start.plusSeconds(360))
    }

    @Test
    fun `should reject zero delay threshold`() {
        assertThrows<IllegalArgumentException> {
            createUseCase(
                telemetry("TRK-000001", 2.0, start),
                delayThreshold = Duration.ZERO
            )
        }
    }

    @Test
    fun `should reject negative delay threshold`() {
        assertThrows<IllegalArgumentException> {
            createUseCase(
                telemetry("TRK-000001", 2.0, start),
                delayThreshold = Duration.ofSeconds(-1)
            )
        }
    }

    @Test
    fun `should reject negative speed threshold`() {
        assertThrows<IllegalArgumentException> {
            createUseCase(
                telemetry("TRK-000001", 2.0, start),
                lowSpeedThresholdKmh = -1.0
            )
        }
    }

    @Test
    fun `should reject non finite speed threshold`() {
        assertThrows<IllegalArgumentException> {
            createUseCase(
                telemetry("TRK-000001", 2.0, start),
                lowSpeedThresholdKmh = Double.NaN
            )
        }
    }

    private fun createUseCase(
        vararg updates: VehicleTelemetry,
        lowSpeedThresholdKmh: Double = 5.0,
        delayThreshold: Duration = Duration.ofMinutes(5)
    ): TrackActiveFleetUseCase =
        TrackActiveFleetUseCase(
            telemetryStreamingService = FakeTelemetryStreamingService(
                updates.toList()
            ),
            lowSpeedThresholdKmh = lowSpeedThresholdKmh,
            delayThreshold = delayThreshold
        )

    private fun telemetry(
        vehicleId: String,
        speedKmh: Double,
        timestamp: Instant
    ) = VehicleTelemetry(
        vehicleId = vehicleId,
        latitude = 31.5,
        longitude = 34.5,
        speedKmh = speedKmh,
        fuelPercentage = 75.0,
        timestamp = timestamp
    )

    private class FakeTelemetryStreamingService(
        private val updates: List<VehicleTelemetry>
    ) : TelemetryStreamingService {

        override fun streamTelemetry(): Flow<VehicleTelemetry> =
            flowOf(*updates.toTypedArray())
    }
}
