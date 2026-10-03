package domain.model

import domain.pricing.DispatchStrategy
import domain.pricing.decorator.PackageComponent
import domain.validator.logisticspackage.CreatePackageValidator
import domain.exception.EntityValidationException
import domain.validator.ResultValidator

data class Package(
    val id: String,
    val weightByKg: Double,
    val priority: String,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
) : PackageComponent {

    lateinit var state: Any

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
