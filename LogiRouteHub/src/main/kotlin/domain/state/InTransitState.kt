package domain.state

import domain.exception.InvalidShipmentStateTransitionException

class InTransitState : ShipmentState {

    override fun assignToVehicle(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package cannot be assigned to a vehicle because it is already in transit."
        )
    }

    override fun markAsInTransit(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package is already in transit."
        )
    }

    override fun markAsDelivered(): ShipmentState {
        return DeliveredState()
    }

    override fun deliveryFailed(): ShipmentState {
        return DeliveryFailedState()
    }
}
