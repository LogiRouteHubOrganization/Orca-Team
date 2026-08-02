package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

interface DispatchStrategy {

    fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double

    fun getStrategyMultiplier(): Double
}