package mainTest.domainTest.stateTest

import com.google.common.truth.Truth.assertThat
import domain.exception.InvalidShipmentStateTransitionException
import domain.state.AssignedToVehicleState
import domain.state.CreatedState
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Test

class CreatedStateTest {

    private val state = CreatedState()

    @Test
    fun `should transition to AssignedToVehicleState when vehicle is assigned`() {
        // Given
        val createdState = state

        // When
        val nextState = createdState.assignToVehicle()

        // Then
        assertThat(nextState).isInstanceOf(AssignedToVehicleState::class.java)
    }

    @Test
    fun `should reject transit before vehicle assignment`() {
        // Given
        val createdState = state

        // When & Then
        assertThrows(InvalidShipmentStateTransitionException::class.java) {
            createdState.markAsInTransit()
        }
    }

    @Test
    fun `should reject delivery while shipment is created`() {
        // Given
        val createdState = state

        // When & Then
        assertThrows(InvalidShipmentStateTransitionException::class.java) {
            createdState.markAsDelivered()
        }
    }

    @Test
    fun `should reject delivery failure while shipment is created`() {
        // Given
        val createdState = state

        // When & Then
        assertThrows(InvalidShipmentStateTransitionException::class.java) {
            createdState.deliveryFailed()
        }
    }
}

