package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

interface DispatchStrategy {

    fun calculateTransitCost(
        logisticsPackage: Package,
        route: Route,
        vehicle: Vehicle
    ): Double

    fun getPriorityMultiplier(): Double

}