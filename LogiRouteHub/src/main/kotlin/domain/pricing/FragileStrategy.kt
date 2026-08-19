package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle


class FragileStrategy : DispatchStrategy {
    private val baseCost = 10.0
    private val safetyFee = 50.0
    private val weightMultiplier = 1.2
    private val distanceMultiplier = 1.3
    private val strategyMultiplier = 1.2

    override fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {

        val weightCost = logisticsPackage.weightByKg * weightMultiplier

        val transportationCost = selectedRoute.distanceKm * assignedVehicle.costPerKm * distanceMultiplier

        val costBeforeStrategyMultiplier = baseCost + safetyFee + weightCost + transportationCost

        return costBeforeStrategyMultiplier * getStrategyMultiplier()
    }

    override fun getStrategyMultiplier(): Double {
        return strategyMultiplier
    }
}
