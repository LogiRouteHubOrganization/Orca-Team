package domain.state

import domain.exception.InvalidShipmentStateTransitionException

class CreatedState : ShipmentState {

    override fun assignToVehicle(): ShipmentState {
        return AssignedToVehicleState()
    }

    override fun markAsInTransit(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot start transit before being assigned to a vehicle."
        )
    }

    override fun markAsDelivered(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot be delivered while it is in Created state."
        )
    }

    override fun deliveryFailed(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot fail delivery while it is in Created state."
        )
    }
}

