package domain.dispatch

import domain.model.Package
import domain.model.Vehicle
import domain.state.ShipmentState

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

    protected open fun updateShipmentState(
        logisticsPackage: Package
    ) {
        val currentState = logisticsPackage.state as ShipmentState

        logisticsPackage.state =
            currentState
                .assignToVehicle()
                .markAsInTransit()
    }


    protected open fun notifyDispatchStatus(
        logisticsPackage: Package,
        vehicle: Vehicle
    ) {
    }
}

