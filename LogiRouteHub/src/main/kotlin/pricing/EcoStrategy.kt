package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

class EcoStrategy : DispatchStrategy {

    override fun calculateTransitCost(
        logisticsPackage: Package,
        route: Route,
        vehicle: Vehicle
    ): Double {

        val baseCost = 5.0
        val weightMultiplier = 0.8
        val distanceMultiplier = 1.0

        return baseCost +
                (logisticsPackage.packageWeightByKg * weightMultiplier) +
                (route.distanceKm * distanceMultiplier)
    }

    override fun getPriorityMultiplier(): Double {
        return 0.8
    }
}