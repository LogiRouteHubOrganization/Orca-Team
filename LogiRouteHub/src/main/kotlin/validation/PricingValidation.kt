package validation

import domain.model.Warehouse
import pricing.EcoStrategy
import pricing.ExpressStrategy
import pricing.FragileStrategy
import pricing.RoutePricingEngine
import java.util.Locale

fun validatePricingStrategies(
    warehousesById: Map<String, Warehouse>
) {

    println()
    println("=== Pricing Validation ===")

    val warehouseForPricing = findWarehouseForPricingValidation(
            warehousesById
        )

    if (warehouseForPricing == null) {

        println("Pricing validation skipped: " + "No warehouse contains a package, route, and vehicle.")
        return
    }
    val logisticsPackage = warehouseForPricing.cargoQueue.first()

    val selectedRoute = warehouseForPricing.outgoingRoutes.first()

    val assignedVehicle = warehouseForPricing.stationedVehicles.first()

    val pricingEngine = RoutePricingEngine(EcoStrategy())

    println()
    println("Warehouse: ${warehouseForPricing.warehouseId}")

    println()
    println("Using EcoStrategy...")
    val ecoCost = pricingEngine.calculatePackageTransitCost(
            logisticsPackage,
            selectedRoute,
            assignedVehicle
        )
    println("Eco Strategy Cost: " + String.format(Locale.US, "%.2f", ecoCost))

    println()
    println("Switching to ExpressStrategy...")

    pricingEngine.updateDispatchStrategy(ExpressStrategy())
    val expressCost = pricingEngine.calculatePackageTransitCost(
            logisticsPackage,
            selectedRoute,
            assignedVehicle
        )

    println("Express Strategy Cost: " + String.format(Locale.US, "%.2f", expressCost))

    println()
    println("Switching to FragileStrategy...")
    pricingEngine.updateDispatchStrategy(FragileStrategy())

    val fragileCost = pricingEngine.calculatePackageTransitCost(
            logisticsPackage,
            selectedRoute,
            assignedVehicle
        )

    println("Fragile Strategy Cost: " + String.format(Locale.US, "%.2f", fragileCost))
    println()
    println("Pricing validation completed.")
}

private fun findWarehouseForPricingValidation(
    warehousesById: Map<String, Warehouse>
): Warehouse? {
    for (warehouse in warehousesById.values) {
        if (
            warehouse.cargoQueue.isNotEmpty() &&
            warehouse.stationedVehicles.isNotEmpty() &&
            warehouse.outgoingRoutes.isNotEmpty()
        ) {
            return warehouse
        }
    }
    return null
}