package domain.model

import domain.exception.EntityValidationException
import domain.pricing.DispatchStrategy
import domain.pricing.decorator.PackageComponent
import domain.state.CreatedState
import domain.state.ShipmentState
import domain.validator.ResultValidator
import domain.validator.logisticspackage.CreatePackageValidator

data class Package(
    val id: String,
    val weightByKg: Double,
    val priority: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
) : PackageComponent {

    var state: ShipmentState = CreatedState()

    init {
        val validationResult = CreatePackageValidator().invoke(
            id = id,
            weightByKg = weightByKg,
            priority = priority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        if (validationResult is ResultValidator.Invalid) {
            throw EntityValidationException(validationResult.errors)
        }
    }

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