package domain.validation

import domain.model.Warehouse
import domain.pricing.DispatchStrategy
import domain.usecase.CalculatePricingUseCase
import kotlin.math.abs

private const val COLD_CHAIN_MULTIPLIER = 1.30
private const val EXPRESS_INSURANCE_FEE = 50.0
private const val PROTECTIVE_FEE = 70.0
private const val RATE_TOLERANCE = 0.001

fun validateDecoratorIntegration(
    warehouseIndex: Map<String, Warehouse>,
    calculatePricingUseCase: CalculatePricingUseCase,
    dispatchStrategy: DispatchStrategy
) {
    val warehouse = findWarehouseForDecoratorValidation(warehouseIndex) ?: return

    val logisticsPackage = warehouse.cargoQueue.first()
    val selectedRoute = warehouse.outgoingRoutes.first()
    val assignedVehicle = warehouse.stationedVehicles.first()

    val baseRate = logisticsPackage.calculateTransitRate(
        selectedRoute,
        assignedVehicle,
        dispatchStrategy
    )

    val decoratedRate = calculatePricingUseCase(
        logisticsPackage = logisticsPackage,
        selectedRoute = selectedRoute,
        assignedVehicle = assignedVehicle,
        dispatchStrategy = dispatchStrategy,
        isFragile = true,
        requiresColdChain = true,
        needsExpressInsurance = true
    )

    val expectedRate = calculateExpectedDecoratedRate(baseRate)

    printDecoratorValidationResult(
        baseRate,
        decoratedRate,
        expectedRate
    )
}

private fun findWarehouseForDecoratorValidation(
    warehouseIndex: Map<String, Warehouse>
): Warehouse? {

    val warehouse = warehouseIndex.values.firstOrNull { warehouse ->
        warehouse.cargoQueue.isNotEmpty() && warehouse.outgoingRoutes.isNotEmpty() &&
                warehouse.stationedVehicles.isNotEmpty()
    }
    if (warehouse == null)
        println("Decorator validation skipped: " + "No warehouse contains the required data.")

    return warehouse
}

private fun calculateExpectedDecoratedRate(
    baseRate: Double
): Double {

    return (baseRate + PROTECTIVE_FEE) * COLD_CHAIN_MULTIPLIER + EXPRESS_INSURANCE_FEE
}

private fun printDecoratorValidationResult(
    baseRate: Double,
    decoratedRate: Double,
    expectedRate: Double
) {
    println()
    println("=== Decorator Integration Validation ===")
    println("Base Rate: $baseRate")
    println("Decorated Rate: $decoratedRate")
    println("Expected Rate: $expectedRate")

    if (
        isCloseTo(actualRate = decoratedRate, expectedRate = expectedRate)
    ) {
        println("Decorator integration validation passed.")
    } else {
        println("Decorator integration validation failed.")
    }
}

private fun isCloseTo(
    actualRate: Double,
    expectedRate: Double
): Boolean {
    return abs(actualRate - expectedRate) < RATE_TOLERANCE
}
