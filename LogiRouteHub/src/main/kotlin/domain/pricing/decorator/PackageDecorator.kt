package domain.pricing.decorator

import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy

abstract class PackageDecorator(
    private val wrappedComponent: PackageComponent
) : PackageComponent {

    override fun calculateTransitRate(
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy
    ): Double {

        return wrappedComponent.calculateTransitRate(
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )
    }
}