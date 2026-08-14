package domain.pricing.decorator

import domain.model.Warehouse
import domain.pricing.EcoStrategy

fun validateDecoratorIntegration(
    warehouseIndex: Map<String, Warehouse>
) {

    val warehouse = warehouseIndex.values.firstOrNull()

    if (warehouse == null) {
        println("Decorator validation skipped: No warehouses available.")
        return
    }

    val logisticsPackage = warehouse.cargoQueue.firstOrNull()

    if (logisticsPackage == null) {
        println("Decorator validation skipped: No packages available.")
        return
    }

    val selectedRoute = warehouse.outgoingRoutes.firstOrNull()

    if (selectedRoute == null) {
        println("Decorator validation skipped: No routes available.")
        return
    }

    val assignedVehicle = warehouse.stationedVehicles.firstOrNull()

    if (assignedVehicle == null) {
        println("Decorator validation skipped: No vehicles available.")
        return
    }

    val dispatchStrategy = EcoStrategy()

    val baseRate = logisticsPackage.calculateTransitRate(
        selectedRoute = selectedRoute,
        assignedVehicle = assignedVehicle,
        dispatchStrategy = dispatchStrategy
    )

    val decoratedPackage = PackageDecoratorComposer().compose(
        packageComponent = logisticsPackage,
        isFragile = true,
        requiresColdChain = true,
        needsExpressInsurance = true
    )

    val decoratedRate = decoratedPackage.calculateTransitRate(
        selectedRoute = selectedRoute,
        assignedVehicle = assignedVehicle,
        dispatchStrategy = dispatchStrategy
    )

    val expectedRate =
        (baseRate + 70.0) * 1.30 + 50.0

    check(decoratedRate == expectedRate) {
        "Decorator integration validation failed."
    }

    println()
    println("=== Decorator Integration Validation ===")
    println("Base Rate: $baseRate")
    println("Decorated Rate: $decoratedRate")
    println("Expected Rate: $expectedRate")
    println("Decorator integration validation passed.")
}