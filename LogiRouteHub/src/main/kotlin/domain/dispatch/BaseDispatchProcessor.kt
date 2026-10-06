package domain.dispatch

import domain.model.Package
import domain.model.Vehicle

abstract class BaseDispatchProcessor {
    fun processDispatch(pkg: Package, vehicle: Vehicle): Boolean {
        if (!validateCargo(pkg, vehicle)) {
            return false
        }
        if (!reserveVehicleCapacity(pkg, vehicle)) {
            return false
        }
        updateShipmentState(pkg)
        notifyDispatchStatus(pkg, vehicle)
        return true
    }

    protected abstract fun validateCargo(pkg: Package, vehicle: Vehicle): Boolean

    protected abstract fun reserveVehicleCapacity(pkg: Package, vehicle: Vehicle): Boolean

    protected abstract fun updateShipmentState(pkg: Package)

    protected open fun notifyDispatchStatus(pkg: Package, vehicle: Vehicle) {
    }
}