package domain.pricing.decorator

import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy


class ExpressInsuranceDecorator(
    wrappedComponent: PackageComponent
) : PackageDecorator(wrappedComponent) {

    private val expressInsuranceFee = 50.0

    override fun calculateTransitRate(
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy
    ): Double {

        val currentTransitRate = super.calculateTransitRate(
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )

        return currentTransitRate + expressInsuranceFee
    }
}
