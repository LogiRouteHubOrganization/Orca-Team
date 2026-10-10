package mainTest.domainTest.stateTest

import com.google.common.truth.Truth.assertThat
import domain.exception.InvalidShipmentStateTransitionException
import domain.state.DeliveredState
import domain.state.DeliveryFailedState
import domain.state.InTransitState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InTransitStateTest {

    @Test
    fun `markAsDelivered should transition package to DeliveredState`() {
        // Given
        val state = InTransitState()

        // When
        val nextState = state.markAsDelivered()

        // Then
        assertThat(nextState).isInstanceOf(DeliveredState::class.java)
    }

    @Test
    fun `deliveryFailed should transition package to DeliveryFailedState`() {
        // Given
        val state = InTransitState()

        // When
        val nextState = state.deliveryFailed()

        // Then
        assertThat(nextState).isInstanceOf(DeliveryFailedState::class.java)
    }

    @Test
    fun `assignToVehicle should reject invalid transition`() {
        // Given
        val state = InTransitState()

        // When
        val transitionCall = { state.assignToVehicle() }

        // Then
        assertThrows<InvalidShipmentStateTransitionException> {
            transitionCall()
        }
    }

    @Test
    fun `markAsInTransit should reject invalid transition`() {
        // Given
        val state = InTransitState()

        // When
        val transitionCall = { state.markAsInTransit() }

        // Then
        assertThrows<InvalidShipmentStateTransitionException> {
            transitionCall()
        }
    }
}
