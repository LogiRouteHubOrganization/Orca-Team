package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

private const val BASE_COST = 20.0
private const val WEIGHT_MULTIPLIER = 2.5
private const val DISTANCE_MULTIPLIER = 3.0
private const val PRIORITY_MULTIPLIER = 1.5

class ExpressStrategy : DispatchStrategy {

    override fun calculateTransitCost(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle
    ): Double {

        val weightCost =
            logisticsPackage.packageWeightByKg *
                    WEIGHT_MULTIPLIER

        val transportationCost =
            selectedRoute.distanceKm *
                    assignedVehicle.costPerKm *
                    DISTANCE_MULTIPLIER

        val costBeforePriority =
            BASE_COST +
                    weightCost +
                    transportationCost

        return costBeforePriority *
                getPriorityMultiplier()
    }

    override fun getPriorityMultiplier(): Double {
        return PRIORITY_MULTIPLIER
    }
}