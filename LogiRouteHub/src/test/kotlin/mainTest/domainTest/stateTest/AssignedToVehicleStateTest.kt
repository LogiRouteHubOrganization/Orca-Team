package mainTest.domainTest.stateTest

import com.google.common.truth.Truth.assertThat
import domain.exception.InvalidShipmentStateTransitionException
import domain.state.AssignedToVehicleState
import domain.state.InTransitState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AssignedToVehicleStateTest {

    private val state = AssignedToVehicleState()

    @Test
    fun `markAsInTransit returns InTransitState`() {
        // When
        val nextState = state.markAsInTransit()

        // Then
        assertThat(nextState).isInstanceOf(InTransitState::class.java)
    }

    @Test
    fun `assignToVehicle throws exception when package is already assigned`() {
        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.assignToVehicle()
        }
    }

    @Test
    fun `markAsDelivered throws exception before package is in transit`() {
        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.markAsDelivered()
        }
    }

    @Test
    fun `deliveryFailed throws exception before package is in transit`() {
        // When & Then
        assertThrows<InvalidShipmentStateTransitionException> {
            state.deliveryFailed()
        }
    }
}
