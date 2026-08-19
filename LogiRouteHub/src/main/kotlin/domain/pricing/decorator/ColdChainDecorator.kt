package domain.pricing.decorator

import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy


class ColdChainDecorator(
    wrappedComponent: PackageComponent
) : PackageDecorator(wrappedComponent) {
    private val COLD_CHAIN_MULTIPLIER = 1.30

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

        return currentTransitRate * COLD_CHAIN_MULTIPLIER
    }
}
