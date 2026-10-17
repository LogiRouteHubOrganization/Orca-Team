package mainTest.domainTest.dispatchTest

import com.google.common.truth.Truth.assertThat
import domain.dispatch.StandardDispatchProcessor
import domain.exception.InvalidCargoException
import domain.exception.VehicleCapacityExceededException
import domain.model.Package
import domain.model.Vehicle
import domain.state.CreatedState
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StandardDispatchProcessorTest {

    private val processor = StandardDispatchProcessor()

    @Test
    fun `should add standard package to vehicle and move it to in transit`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val vehicle = mockk<Vehicle>(relaxed = true)

        every { logisticsPackage.weightByKg } returns 20.0

        every { logisticsPackage.id } returns "PKG-000001"

        every { logisticsPackage.state } returns CreatedState()

        every { logisticsPackage.state = any() } just Runs

        every { vehicle.maxCapacityKg } returns 100.0

        every { vehicle.id } returns "TRK-000001"

        every { vehicle.addPackage(logisticsPackage) } just Runs
        every { logisticsPackage.assignToVehicle() } just Runs
        every { logisticsPackage.markAsInTransit() } just Runs

        // When
        processor.processDispatch(
            logisticsPackage = logisticsPackage,
            vehicle = vehicle
        )
        // Then
        verify(exactly = 1) {
            logisticsPackage.assignToVehicle()
            logisticsPackage.markAsInTransit()
            vehicle.addPackage(logisticsPackage)
        }
    }

    @Test
    fun `should add low priority package to vehicle and move it to in transit`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val vehicle = mockk<Vehicle>(relaxed = true)

        every { logisticsPackage.weightByKg } returns 15.0

        every { logisticsPackage.id } returns "PKG-000002"

        every { logisticsPackage.priority } returns "LOW"

        every { logisticsPackage.state } returns CreatedState()

        every { logisticsPackage.state = any() } just Runs

        every { vehicle.maxCapacityKg } returns 100.0

        every { vehicle.id } returns "TRK-000001"

        every { logisticsPackage.assignToVehicle() } just Runs
        every { logisticsPackage.markAsInTransit() } just Runs
        every { vehicle.addPackage(logisticsPackage) } just Runs

        // When
        processor.processDispatch(
            logisticsPackage = logisticsPackage,
            vehicle = vehicle
        )

        // Then
        verify(exactly = 1) {
            vehicle.addPackage(logisticsPackage)
            logisticsPackage.assignToVehicle()
            logisticsPackage.markAsInTransit()

        }

        @Test
        fun `should throw invalid cargo exception when package weight is not positive`() {
            // Given
            val logisticsPackage = mockk<Package>()
            val vehicle = mockk<Vehicle>()

            every { logisticsPackage.weightByKg } returns 0.0

            every { logisticsPackage.id } returns "PKG-000001"

            every { vehicle.maxCapacityKg } returns 100.0

            // When
            val exception = assertThrows<InvalidCargoException> {
                processor.processDispatch(
                    logisticsPackage = logisticsPackage,
                    vehicle = vehicle
                )
            }
            // Then
            assertThat(exception.message)
                .contains("must have a positive weight")

            verify(exactly = 0) {
                vehicle.addPackage(any())
            }
        }

        @Test
        fun `should throw capacity exception when package exceeds vehicle capacity`() {
            // Given
            val logisticsPackage = mockk<Package>()
            val vehicle = mockk<Vehicle>()

            every { logisticsPackage.weightByKg } returns 120.0

            every { logisticsPackage.id } returns "PKG-000001"

            every { vehicle.maxCapacityKg } returns 100.0

            every { vehicle.id } returns "TRK-000001"

            // When
            val exception = assertThrows<VehicleCapacityExceededException> {
                processor.processDispatch(
                    logisticsPackage = logisticsPackage,
                    vehicle = vehicle
                )
            }

            // Then
            assertThat(exception.message)
                .contains("does not have enough capacity")

            verify(exactly = 0) {
                vehicle.addPackage(any())
            }
        }
    }
}

