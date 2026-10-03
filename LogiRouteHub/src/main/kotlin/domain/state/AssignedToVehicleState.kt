package domain.state

import domain.exception.InvalidShipmentStateTransitionException

class AssignedToVehicleState : ShipmentState {

    override fun assignToVehicle(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot be assigned to a vehicle because it is already assigned."
        )
    }

    override fun markAsInTransit(): ShipmentState {
        return InTransitState()
    }

    override fun markAsDelivered(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot be delivered before it is in transit."
        )
    }

    override fun deliveryFailed(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot fail delivery before it is in transit."
        )
    }
}
