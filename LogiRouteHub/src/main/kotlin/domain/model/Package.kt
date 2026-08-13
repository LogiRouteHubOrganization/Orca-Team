package domain.model

import domain.pricing.DispatchStrategy
import domain.pricing.decorator.PackageComponent

data class Package(
    val id: String,
    val weightByKg: Double,
    val priority: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
) : PackageComponent {

    override fun calculateTransitRate(
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy
    ): Double {

        return dispatchStrategy.calculateTransitCost(
            logisticsPackage = this,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle
        )
    }
}