package domain.dispatch

abstract class BaseDispatchProcessor {

    fun processDispatch() {
        validateCargo()
        reserveVehicleCapacity()
        updateShipmentState()
        notifyDispatchStatus()
    }

    protected abstract fun validateCargo()

    protected abstract fun reserveVehicleCapacity()

    protected abstract fun updateShipmentState()


    protected open fun notifyDispatchStatus() {

    }
}
