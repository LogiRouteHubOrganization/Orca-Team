package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

class FragileStrategy : DispatchStrategy {

    override fun calculateTransitCost(
        logisticsPackage: Package,
        route: Route,
        vehicle: Vehicle
    ): Double {

        val baseCost = 10.0
        val safetyFee = 50.0
        val weightMultiplier = 1.2
        val distanceMultiplier = 1.3

        return baseCost +
                safetyFee +
                (logisticsPackage.packageWeightByKg * weightMultiplier) +
                (route.distanceKm * distanceMultiplier)
    }

    override fun getPriorityMultiplier(): Double {
        return 1.2
    }
}