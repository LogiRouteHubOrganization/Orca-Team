package domain.dispatch

import domain.model.Package
import domain.model.Vehicle
import domain.state.ShipmentState

class StandardDispatchProcessor : BaseDispatchProcessor() {

    override fun validateCargo(pkg: Package, vehicle: Vehicle): Boolean {
        return pkg.weightByKg > 0 && vehicle.maxCapacityKg >= pkg.weightByKg
    }

    override fun reserveVehicleCapacity(pkg: Package, vehicle: Vehicle): Boolean {
        return true
    }

    override fun updateShipmentState(pkg: Package) {
        val currentState = pkg.state as ShipmentState
        pkg.state = currentState.assignToVehicle().markAsInTransit()
    }

    override fun notifyDispatchStatus(pkg: Package, vehicle: Vehicle) {
        println("STANDARD DISPATCH: Package ${pkg.id} assigned and in transit via vehicle ${vehicle.id}.")
    }
}