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

        // When
        val transitionCall = { state.assignToVehicle() }

        // Then
        assertThrows<InvalidShipmentStateTransitionException> {
            transitionCall()
        }
    }

    @Test
    fun `markAsInTransit should reject transition from delivered state`() {
        // Given
        val state = DeliveredState()

        // When
        val transitionCall = { state.markAsInTransit() }

        // Then
        assertThrows<InvalidShipmentStateTransitionException> {
            transitionCall()
        }
    }

    @Test
    fun `markAsDelivered should reject transition because package is already delivered`() {
        // Given
        val state = DeliveredState()

        // When
        val transitionCall = { state.markAsDelivered() }

        // Then
        assertThrows<InvalidShipmentStateTransitionException> {
            transitionCall()
        }
    }

    @Test
    fun `deliveryFailed should reject transition from delivered state`() {
        // Given
        val state = DeliveredState()

        // When
        val transitionCall = { state.deliveryFailed() }

        // Then
        assertThrows<InvalidShipmentStateTransitionException> {
            transitionCall()
        }
    }
}

