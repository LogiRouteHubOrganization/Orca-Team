package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

class RoutePricingEngine(

    private var currentDispatchStrategy: DispatchStrategy

) {

    fun updateDispatchStrategy(
        newDispatchStrategy: DispatchStrategy
    ) {
        currentDispatchStrategy = newDispatchStrategy
    }
    fun calculatePackageTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {
        return currentDispatchStrategy.calculateTransitCost(
            logisticsPackage,
            selectedRoute,
            assignedVehicle
        )
    }
}