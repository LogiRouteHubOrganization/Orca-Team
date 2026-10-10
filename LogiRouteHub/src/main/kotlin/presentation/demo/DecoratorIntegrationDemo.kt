package presentation.demo

import domain.model.Warehouse
import domain.model.request.CalculatePricingRequest
import domain.pricing.DispatchStrategy
import domain.usecase.CalculatePricingUseCase

fun demonstrateDecoratorIntegration(
    warehouseIndex: Map<String, Warehouse>,
    calculatePricingUseCase: CalculatePricingUseCase,
    dispatchStrategy: DispatchStrategy
) {
    val warehouse = findWarehouseForDecoratorDemo(
        warehouseIndex
    ) ?: return

    val logisticsPackage = warehouse.cargoQueue.first()
    val selectedRoute = warehouse.outgoingRoutes.first()
    val assignedVehicle = warehouse.stationedVehicles.first()

    val baseRate = logisticsPackage.calculateTransitRate(
        selectedRoute,
        assignedVehicle,
        dispatchStrategy
    )

    val pricingRequest = CalculatePricingRequest(
        logisticsPackage = logisticsPackage,
        selectedRoute = selectedRoute,
        assignedVehicle = assignedVehicle,
        dispatchStrategy = dispatchStrategy,
        isFragile = true,
        requiresColdChain = true,
        needsExpressInsurance = true
    )

    val decoratedRate = calculatePricingUseCase(
        pricingRequest
    ).getOrElse { exception ->
        println(
            "Decorator pricing demo failed: ${exception.message}"
        )
        return
    }

    printDecoratorDemoResult(
        warehouseId = warehouse.id,
        packageId = logisticsPackage.id,
        routeId = selectedRoute.id,
        vehicleId = assignedVehicle.id,
        baseRate = baseRate,
        decoratedRate = decoratedRate
    )
}

private fun findWarehouseForDecoratorDemo(
    warehouseIndex: Map<String, Warehouse>
): Warehouse? {
    val warehouse = warehouseIndex.values.firstOrNull { warehouse ->
        warehouse.cargoQueue.isNotEmpty() &&
                warehouse.outgoingRoutes.isNotEmpty() &&
                warehouse.stationedVehicles.isNotEmpty()
    }

    if (warehouse == null) {
        println(
            "Decorator demo skipped: " +
                    "No warehouse contains the required data."
        )
    }

    return warehouse
}

private fun printDecoratorDemoResult(
    warehouseId: String,
    packageId: String,
    routeId: String,
    vehicleId: String,
    baseRate: Double,
    decoratedRate: Double
) {
    println()
    println("=== Decorator Pricing Demo ===")
    println("Warehouse: $warehouseId")
    println("Package: $packageId")
    println("Route: $routeId")
    println("Vehicle: $vehicleId")
    println("Base Rate: $baseRate")
    println("Decorators Applied:")
    println("- Fragile Protection")
    println("- Cold Chain")
    println("- Express Insurance")
    println("Final Decorated Rate: $decoratedRate")
}
