package domain.dispatch

import domain.model.Package
import domain.model.Vehicle
import domain.state.ShipmentState

class ExpressDispatchProcessor : BaseDispatchProcessor() {

    override fun validateCargo(pkg: Package, vehicle: Vehicle): Boolean {
        val isExpressPriority = pkg.priority == "URGENT" || pkg.priority == "EXPRESS"
        return isExpressPriority && vehicle.maxCapacityKg >= pkg.weightByKg
    }

    override fun reserveVehicleCapacity(pkg: Package, vehicle: Vehicle): Boolean {
        return true
    }

    override fun updateShipmentState(pkg: Package) {
        val currentState = pkg.state as ShipmentState
        pkg.state = currentState.assignToVehicle().markAsInTransit()
    }

    override fun notifyDispatchStatus(pkg: Package, vehicle: Vehicle) {
        println("EXPRESS DISPATCH [PRIORITY]: Package ${pkg.id} prioritized, assigned, and in transit via vehicle ${vehicle.id}.")
    }
}