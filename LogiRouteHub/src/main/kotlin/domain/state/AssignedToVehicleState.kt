package domain.state

import domain.exception.InvalidShipmentStateTransitionException

class AssignedToVehicleState : ShipmentState  {

    override fun assignToVehicle(): ShipmentState =
        throw InvalidShipmentStateTransitionException("Not implemented yet")

    override fun markAsInTransit(): ShipmentState =
        throw InvalidShipmentStateTransitionException("Not implemented yet")

    override fun markAsDelivered(): ShipmentState =
        throw InvalidShipmentStateTransitionException("Not implemented yet")

    override fun deliveryFailed(): ShipmentState =
        throw InvalidShipmentStateTransitionException("Not implemented yet")
}
