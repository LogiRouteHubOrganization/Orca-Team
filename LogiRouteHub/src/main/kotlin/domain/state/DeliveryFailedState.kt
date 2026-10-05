package domain.state

import domain.exception.InvalidShipmentStateTransitionException

class DeliveryFailedState : ShipmentState {

    override fun assignToVehicle(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package with failed delivery cannot be assigned to a vehicle."
        )
    }

    override fun markAsInTransit(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package with failed delivery cannot return to transit."
        )
    }

    override fun markAsDelivered(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "A package with failed delivery cannot be delivered."
        )
    }

    override fun deliveryFailed(): ShipmentState {
        throw InvalidShipmentStateTransitionException(
            "Delivery has already failed for this package."
        )
    }
}