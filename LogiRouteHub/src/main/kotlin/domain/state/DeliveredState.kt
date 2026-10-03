package domain.state

import domain.exception.InvalidShipmentStateTransitionException

class DeliveredState : ShipmentState {

    override fun assignToVehicle(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A delivered package cannot be assigned to a vehicle."
        )
    }

    override fun markAsInTransit(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A delivered package cannot return to transit."
        )
    }

    override fun markAsDelivered(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package is already delivered."
        )
    }

    override fun deliveryFailed(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A delivered package cannot transition to delivery failed."
        )
    }
}