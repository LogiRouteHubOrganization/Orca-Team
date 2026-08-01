package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

private const val BASE_COST = 10.0
private const val SAFETY_FEE = 50.0
private const val WEIGHT_MULTIPLIER = 1.2
private const val DISTANCE_MULTIPLIER = 1.3
private const val PRIORITY_MULTIPLIER = 1.2

class FragileStrategy : DispatchStrategy {

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
                    SAFETY_FEE +
                    weightCost +
                    transportationCost

        return costBeforePriority *
                getPriorityMultiplier()
    }

    override fun getPriorityMultiplier(): Double {
        return PRIORITY_MULTIPLIER
    }
}