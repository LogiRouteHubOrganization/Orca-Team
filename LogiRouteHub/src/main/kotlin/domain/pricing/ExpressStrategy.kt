package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle


class ExpressStrategy : DispatchStrategy {

    private val baseCost = 18.0
    private val weightMultiplier = 1.4
    private val distanceMultiplier = 1.25
    private val strategyMultiplier = 1.20

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
