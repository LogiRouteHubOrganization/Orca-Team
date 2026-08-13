package validation

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.Warehouse
import domain.pricing.*
import domain.pricing.decorator.PackageComponent
import java.util.*

fun validatePricingStrategies(
    warehousesById: Map<String, Warehouse>
) {

    println()
    println("=== Pricing Validation ===")

    val warehouse = findWarehouseForPricingValidation(warehousesById)

    if (warehouse == null) {
        println(
            "Pricing validation skipped: " +
                    "No warehouse contains a package, route, and vehicle."
        )
        return
    }

    val logisticsPackage = warehouse.cargoQueue.firstOrNull()
    val route = warehouse.outgoingRoutes.firstOrNull()
    val vehicle = warehouse.stationedVehicles.firstOrNull()

    if (
        logisticsPackage == null ||
        route == null ||
        vehicle == null
    ) {
        println("Pricing validation failed: Missing required data.")
        return
    }

    println()
    println("Warehouse: ${warehouse.id}")

    val pricingEngine = RoutePricingEngine(EcoStrategy())

    val strategies = listOf(
        Pair("EcoStrategy", EcoStrategy()),
        Pair("ExpressStrategy", ExpressStrategy()),
        Pair("FragileStrategy", FragileStrategy())
    )
    val basePackageComponent: PackageComponent =
        logisticsPackage

    val baseTransitRate =
        basePackageComponent.calculateTransitRate(
            selectedRoute = route,
            assignedVehicle = vehicle,
            dispatchStrategy = EcoStrategy()
        )

    println()
    println(
        "Base Package Transit Rate: " +
                String.format(
                    Locale.US,
                    "%.2f",
                    baseTransitRate
                )
    )
    for ((strategyName, strategy) in strategies) {
        validateStrategy(
            pricingEngine,
            strategyName,
            strategy,
            logisticsPackage,
            route,
            vehicle
        )
    }

    println()
    println("Pricing validation completed.")
}

private fun validateStrategy(
    pricingEngine: RoutePricingEngine,
    strategyName: String,
    strategy: DispatchStrategy,
    logisticsPackage: Package,
    route: Route,
    vehicle: Vehicle
) {

    println()
    println("Using $strategyName...")

    pricingEngine.updateDispatchStrategy(strategy)

    val cost = pricingEngine.calculatePackageTransitCost(
        logisticsPackage,
        route,
        vehicle
    )

    println(
        "$strategyName Cost: " +
                String.format(Locale.US, "%.2f", cost)
    )
}

private fun findWarehouseForPricingValidation(
    warehousesById: Map<String, Warehouse>
): Warehouse? {

    return warehousesById.values.firstOrNull { warehouse ->

        warehouse.cargoQueue.isNotEmpty() &&
                warehouse.outgoingRoutes.isNotEmpty() &&
                warehouse.stationedVehicles.isNotEmpty()
    }
}