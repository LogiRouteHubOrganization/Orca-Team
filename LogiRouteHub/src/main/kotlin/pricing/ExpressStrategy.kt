package pricing

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle

class ExpressStrategy : DispatchStrategy {

    override fun calculateTransitCost(
        logisticsPackage: Package,
        route: Route,
        vehicle: Vehicle
    ): Double  {

            val baseCost = 20.0
            val weightMultiplier = 2.5
            val distanceMultiplier = 3.0

        return baseCost +
                (logisticsPackage.packageWeightByKg * weightMultiplier) +
                (route.distanceKm * distanceMultiplier)
        }
    override fun getPriorityMultiplier(): Double {
        return 1.5
    }

}