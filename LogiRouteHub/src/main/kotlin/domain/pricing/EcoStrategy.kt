package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle


class EcoStrategy : DispatchStrategy {

    private val baseCost = 8.0
    private val weightMultiplier = 0.6
    private val distanceMultiplier = 0.9
    private val strategyMultiplier = 0.95

    override fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {

        val weightCost = logisticsPackage.weightByKg * weightMultiplier

        val transportationCost = selectedRoute.distanceKm * assignedVehicle.costPerKm * distanceMultiplier

        val costBeforeStrategyMultiplier = baseCost + weightCost + transportationCost

        return costBeforeStrategyMultiplier * getStrategyMultiplier()
    }

    override fun getStrategyMultiplier(): Double {
        return strategyMultiplier
    }
}
