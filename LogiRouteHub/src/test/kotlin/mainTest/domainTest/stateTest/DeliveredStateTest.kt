package mainTest.domainTest.stateTest

import domain.exception.InvalidShipmentStateTransitionException
import domain.state.DeliveredState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeliveredStateTest {

    @Test
    fun `assignToVehicle should reject transition from delivered state`() {
        // Given
        val state = DeliveredState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.assignToVehicle()
        }
    }

    @Test
    fun `markAsInTransit should reject transition from delivered state`() {
        // Given
        val state = DeliveredState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.markAsInTransit()
        }
    }

    @Test
    fun `markAsDelivered should reject transition because package is already delivered`() {
        // Given
        val state = DeliveredState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.markAsDelivered()
        }
    }

    @Test
    fun `deliveryFailed should reject transition from delivered state`() {
        // Given
        val state = DeliveredState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.deliveryFailed()
        }
    }
}
