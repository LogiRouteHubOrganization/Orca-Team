package domain.dispatch

import domain.model.Package
import domain.model.Vehicle

abstract class BaseDispatchProcessor {

    fun processDispatch(
        logisticsPackage: Package,
        vehicle: Vehicle
    ) {
        validateCargo(logisticsPackage, vehicle)
        reserveVehicleCapacity(logisticsPackage, vehicle)
        updateShipmentState(logisticsPackage)
        notifyDispatchStatus(logisticsPackage, vehicle)
    }

    protected abstract fun validateCargo(
        logisticsPackage: Package,
        vehicle: Vehicle
    )

    protected abstract fun reserveVehicleCapacity(
        logisticsPackage: Package,
        vehicle: Vehicle
    )

    protected abstract fun updateShipmentState(
        logisticsPackage: Package
    )

    protected open fun notifyDispatchStatus(
        logisticsPackage: Package,
        vehicle: Vehicle
    ) {
    }
}
