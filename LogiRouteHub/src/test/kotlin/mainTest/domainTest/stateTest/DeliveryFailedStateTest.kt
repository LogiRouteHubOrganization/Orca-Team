package mainTest.domainTest.stateTest

import domain.exception.InvalidShipmentStateTransitionException
import domain.state.DeliveryFailedState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeliveryFailedStateTest {

    @Test
    fun `assignToVehicle should reject transition from delivery failed state`() {
        // Given
        val state = DeliveryFailedState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.assignToVehicle()
        }
    }

    @Test
    fun `markAsInTransit should reject transition from delivery failed state`() {
        // Given
        val state = DeliveryFailedState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.markAsInTransit()
        }
    }

    @Test
    fun `markAsDelivered should reject transition from delivery failed state`() {
        // Given
        val state = DeliveryFailedState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.markAsDelivered()
        }
    }

    @Test
    fun `deliveryFailed should reject transition because delivery has already failed`() {
        // Given
        val state = DeliveryFailedState()

        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.deliveryFailed()
        }
    }
}