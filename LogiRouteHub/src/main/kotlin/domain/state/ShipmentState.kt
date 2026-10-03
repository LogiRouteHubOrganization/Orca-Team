package domain.state

interface ShipmentState {

    fun assignToVehicle(): ShipmentState

    fun markAsInTransit(): ShipmentState

    fun markAsDelivered(): ShipmentState

    fun deliveryFailed(): ShipmentState
}
