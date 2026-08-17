package validation

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.Warehouse
import domain.pricing.DispatchStrategy
import domain.pricing.RoutePricingEngine
import domain.pricing.decorator.PackageComponent
import domain.pricing.decorator.PackageDecorator
import java.util.*

private const val DECIMAL_FORMAT = "%.2f"

fun validatePricingStrategies(
    warehousesById: Map<String, Warehouse>,
    pricingEngine: RoutePricingEngine,
    ecoStrategy: DispatchStrategy,
    expressStrategy: DispatchStrategy,
    fragileStrategy: DispatchStrategy
) {

    println()
    println("=== Pricing Validation ===")

    val warehouse = findWarehouseForPricingValidation(warehousesById)
    if (warehouse == null) {
        println("Pricing validation skipped: " + "No warehouse contains a package, route, and vehicle.")
        return
    }
    val logisticsPackage = warehouse.cargoQueue.firstOrNull()
    val route = warehouse.outgoingRoutes.firstOrNull()
    val vehicle = warehouse.stationedVehicles.firstOrNull()

    if (logisticsPackage == null || route == null || vehicle == null) {
        println("Pricing validation failed: Missing required data.")
        return
    }

    println()
    println(
        "Warehouse: ${warehouse.id}"
    )

    val basePackageComponent: PackageComponent =
        logisticsPackage

    val baseTransitRate =
        basePackageComponent.calculateTransitRate(
            selectedRoute = route,
            assignedVehicle = vehicle,
            dispatchStrategy = ecoStrategy
        )

    println()
    println(
        "Base Package Transit Rate: " +
                String.format(
                    Locale.US,
                    DECIMAL_FORMAT,
                    baseTransitRate
                )
    )

    val baseDecorator = object : PackageDecorator(
        wrappedComponent = basePackageComponent
    ) {}

    val decoratedBaseTransitRate =
        baseDecorator.calculateTransitRate(
            selectedRoute = route,
            assignedVehicle = vehicle,
            dispatchStrategy = ecoStrategy
        )

    println(
        "Base Decorator Transit Rate: " + String.format(
            Locale.US,
            DECIMAL_FORMAT,
            decoratedBaseTransitRate
        )
    )

    validateStrategy(
        pricingEngine = pricingEngine,
        strategyName = "EcoStrategy",
        strategy = ecoStrategy,
        logisticsPackage = logisticsPackage,
        route = route,
        vehicle = vehicle
    )

    validateStrategy(
        pricingEngine = pricingEngine,
        strategyName = "ExpressStrategy",
        strategy = expressStrategy,
        logisticsPackage = logisticsPackage,
        route = route,
        vehicle = vehicle
    )

    validateStrategy(
        pricingEngine = pricingEngine,
        strategyName = "FragileStrategy",
        strategy = fragileStrategy,
        logisticsPackage = logisticsPackage,
        route = route,
        vehicle = vehicle
    )
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
        "$strategyName Cost: " + String.format(
            Locale.US,
            DECIMAL_FORMAT,
            cost
        )
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