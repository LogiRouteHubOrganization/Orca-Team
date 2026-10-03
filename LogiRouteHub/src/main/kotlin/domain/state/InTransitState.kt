package domain.state

class InTransitState : ShipmentState {
    override fun assignToVehicle(): ShipmentState {
        TODO("Implementation will be added in the corresponding task")
    }

    override fun markAsInTransit(): ShipmentState {
        TODO("Implementation will be added in the corresponding task")
    }

    override fun markAsDelivered(): ShipmentState {
        TODO("Implementation will be added in the corresponding task")
    }

    override fun deliveryFailed(): ShipmentState {
        TODO("Implementation will be added in the corresponding task")
    }
}
