package domain.pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle


class FragileStrategy : DispatchStrategy {
    private val BASE_COST = 10.0
    private val SAFETY_FEE = 50.0
    private val WEIGHT_MULTIPLIER = 1.2
    private val DISTANCE_MULTIPLIER = 1.3
    private val STRATEGY_MULTIPLIER = 1.2

    override fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {

        val weightCost = logisticsPackage.weightByKg * WEIGHT_MULTIPLIER

        val transportationCost = selectedRoute.distanceKm * assignedVehicle.costPerKm * DISTANCE_MULTIPLIER

        val costBeforeStrategyMultiplier = BASE_COST + SAFETY_FEE + weightCost + transportationCost

        return costBeforeStrategyMultiplier * getStrategyMultiplier()
    }

    override fun getStrategyMultiplier(): Double {
        return STRATEGY_MULTIPLIER
    }
}
