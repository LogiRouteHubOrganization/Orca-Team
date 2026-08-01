package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

private const val BASE_COST = 18.0
private const val WEIGHT_MULTIPLIER = 1.4
private const val DISTANCE_MULTIPLIER = 1.25
private const val STRATEGY_MULTIPLIER = 1.20

class ExpressStrategy : DispatchStrategy {

    override fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {

        val weightCost = logisticsPackage.packageWeightByKg * WEIGHT_MULTIPLIER

        val transportationCost = selectedRoute.distanceKm * assignedVehicle.costPerKm * DISTANCE_MULTIPLIER

        val costBeforeStrategyMultiplier = BASE_COST + weightCost + transportationCost

        return costBeforeStrategyMultiplier * getStrategyMultiplier()
    }

    override fun getStrategyMultiplier(): Double {
        return STRATEGY_MULTIPLIER
    }
}