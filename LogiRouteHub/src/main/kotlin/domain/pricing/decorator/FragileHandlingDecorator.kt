package domain.pricing.decorator

import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy

private const val PROTECTIVE_FEE = 70.0

class FragileHandlingDecorator(
    wrappedComponent: PackageComponent
) : PackageDecorator(wrappedComponent) {

    override fun calculateTransitRate(
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy
    ): Double {

        val baseTransitRate = super.calculateTransitRate(
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )

        return baseTransitRate + PROTECTIVE_FEE
    }
}