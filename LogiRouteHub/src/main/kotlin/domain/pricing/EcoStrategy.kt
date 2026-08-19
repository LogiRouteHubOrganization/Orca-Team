package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle


class EcoStrategy : DispatchStrategy {

    private val BASE_COST = 8.0
    private val WEIGHT_MULTIPLIER = 0.6
    private val DISTANCE_MULTIPLIER = 0.9
    private val STRATEGY_MULTIPLIER = 0.95

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
