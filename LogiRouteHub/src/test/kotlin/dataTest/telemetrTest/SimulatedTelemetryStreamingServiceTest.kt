package dataTest.serviceTest

import com.google.common.truth.Truth
import data.telemetry.SimulatedTelemetryStreamingService
import domain.model.VehicleTelemetry
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class SimulatedTelemetryStreamingServiceTest {

    @Test
    fun `should emit telemetry for configured vehicles`() = runTest {
        val service = SimulatedTelemetryStreamingService(
            vehicleIds = listOf("TRK-000001", "TRK-000002"),
            intervalMillis = 100L,
            telemetryFactory = { vehicleId, _ ->
                createTelemetry(vehicleId)
            }
        )

        val updates = service.streamTelemetry()
            .take(4)
            .toList()

        Truth.assertThat(updates).hasSize(4)

        Truth.assertThat(updates.map { it.vehicleId }).containsExactly(
            "TRK-000001",
            "TRK-000002",
            "TRK-000001",
            "TRK-000002"
        ).inOrder()
    }

    @Test
    fun `should start a new collection for each collector`() = runTest {
        var generatedCount = 0

        val service = SimulatedTelemetryStreamingService(
            vehicleIds = listOf("TRK-000001"),
            intervalMillis = 100L,
            telemetryFactory = { vehicleId, _ ->
                generatedCount++
                createTelemetry(vehicleId)
            }
        )

        service.streamTelemetry().take(1).toList()
        service.streamTelemetry().take(1).toList()

        Truth.assertThat(generatedCount).isEqualTo(2)
    }

    @Test
    fun `should reject an empty vehicle list`() {
        Assertions.assertThrows(
            IllegalArgumentException::class.java
        ) {
            SimulatedTelemetryStreamingService(vehicleIds = emptyList())
        }
    }

    private fun createTelemetry(vehicleId: String) =
        VehicleTelemetry(
            vehicleId = vehicleId,
            latitude = 31.5,
            longitude = 34.5,
            speedKmh = 60.0,
            fuelPercentage = 75.0,
            timestamp = Instant.parse("2026-10-16T08:00:00Z")
        )
}