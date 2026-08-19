package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle


class ExpressStrategy : DispatchStrategy {

    private val BASE_COST = 18.0
    private val WEIGHT_MULTIPLIER = 1.4
    private val DISTANCE_MULTIPLIER = 1.25
    private val STRATEGY_MULTIPLIER = 1.20

    override fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {

        val weightCost = logisticsPackage.weightByKg * WEIGHT_MULTIPLIER

        val transportationCost = selectedRoute.distanceKm * assignedVehicle.costPerKm * DISTANCE_MULTIPLIER

        val costBeforeStrategyMultiplier = BASE_COST + weightCost + transportationCost

        return costBeforeStrategyMultiplier * getStrategyMultiplier()
    }

    override fun getStrategyMultiplier(): Double {
        return STRATEGY_MULTIPLIER
    }
}
