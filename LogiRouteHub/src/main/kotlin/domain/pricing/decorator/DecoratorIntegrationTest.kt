package domain.pricing.decorator

import domain.model.Warehouse
import domain.pricing.EcoStrategy
private const val COLD_CHAIN_MULTIPLIER = 1.30
private const val EXPRESS_INSURANCE_FEE = 50.0
private const val PROTECTIVE_FEE = 70.0
private const val RATE_TOLERANCE = 0.001
fun validateDecoratorIntegration(
    warehouseIndex: Map<String, Warehouse>
) {

    val warehouse = warehouseIndex.values.firstOrNull()

        ?: run {
        println("Decorator validation skipped: No warehouses available.")
        return
    }

    val logisticsPackage = warehouse.cargoQueue.firstOrNull()

        ?: run {
        println("Decorator validation skipped: No packages available.")
        return
    }

    val selectedRoute = warehouse.outgoingRoutes.firstOrNull()

        ?: run {
        println("Decorator validation skipped: No routes available.")
        return
    }

    val assignedVehicle = warehouse.stationedVehicles.firstOrNull()

        ?: run {
        println("Decorator validation skipped: No vehicles available.")
        return
    }

    val dispatchStrategy = EcoStrategy()

    val baseRate = logisticsPackage.calculateTransitRate(
        selectedRoute = selectedRoute,
        assignedVehicle = assignedVehicle,
        dispatchStrategy = dispatchStrategy
    )

    val decoratedPackage = PackageDecoratorComposer().applyOptionalDecorators(
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
        (baseRate + PROTECTIVE_FEE) *COLD_CHAIN_MULTIPLIER + EXPRESS_INSURANCE_FEE




     fun isCloseTo(
        actualRate: Double,
        expectedRate: Double
    ): Boolean {
        return kotlin.math.abs(actualRate - expectedRate) < RATE_TOLERANCE
    }
    check(isCloseTo(decoratedRate, expectedRate)) {
        "Decorator integration validation failed."
    }
    println()
    println("=== Decorator Integration Validation ===")
    println("Base Rate: $baseRate")
    println("Decorated Rate: $decoratedRate")
    println("Expected Rate: $expectedRate")
    println("Decorator integration validation passed.")
}