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
    val warehouse = findWarehouseForPricingValidation(warehousesById) ?: return
    validateBasePricing(
        warehouse = warehouse,
        ecoStrategy = ecoStrategy
    )
    validateAvailableStrategies(
        warehouse = warehouse,
        pricingEngine = pricingEngine,
        ecoStrategy = ecoStrategy,
        expressStrategy = expressStrategy,
        fragileStrategy = fragileStrategy
    )
    println()
    println("Pricing validation completed.")
}

private fun validateBasePricing(
    warehouse: Warehouse,
    ecoStrategy: DispatchStrategy
) {
    val logisticsPackage = warehouse.cargoQueue.firstOrNull()
    val route = warehouse.outgoingRoutes.firstOrNull()
    val vehicle = warehouse.stationedVehicles.firstOrNull()

    if (logisticsPackage == null || route == null || vehicle == null) {
        println("Pricing validation failed: Missing required data.")
        return
    }
    println()
    println("Warehouse: ${warehouse.id}")

    val basePackageComponent: PackageComponent = logisticsPackage

    val baseTransitRate = basePackageComponent.calculateTransitRate(
        route,
        vehicle,
        ecoStrategy
    )
    printBaseTransitRate(baseTransitRate)
    validateBaseDecorator(
        basePackageComponent,
        route,
        vehicle,
        ecoStrategy
    )
}

private fun printBaseTransitRate(
    baseTransitRate: Double
) {

    println()
    println(
        "Base Package Transit Rate: " + String.format(
            Locale.US,
            DECIMAL_FORMAT,
            baseTransitRate
        )
    )
}

private fun validateBaseDecorator(
    basePackageComponent: PackageComponent,
    route: Route,
    vehicle: Vehicle,
    ecoStrategy: DispatchStrategy
) {
    val baseDecorator = object : PackageDecorator(
        wrappedComponent = basePackageComponent
    ) {}

    val decoratedBaseTransitRate = baseDecorator.calculateTransitRate(
        route,
        vehicle,
        ecoStrategy
    )
    println(
        "Base Decorator Transit Rate: " + String.format(
            Locale.US,
            DECIMAL_FORMAT,
            decoratedBaseTransitRate
        )
    )
}

private fun validateAvailableStrategies(
    warehouse: Warehouse,
    pricingEngine: RoutePricingEngine,
    ecoStrategy: DispatchStrategy,
    expressStrategy: DispatchStrategy,
    fragileStrategy: DispatchStrategy
) {

    val logisticsPackage = warehouse.cargoQueue.firstOrNull() ?: return
    val route = warehouse.outgoingRoutes.firstOrNull() ?: return
    val vehicle = warehouse.stationedVehicles.firstOrNull() ?: return

    validateStrategy(
        pricingEngine,
        "EcoStrategy",
        ecoStrategy,
        logisticsPackage,
        route,
        vehicle
    )
    validateStrategy(
        pricingEngine,
        "ExpressStrategy",
        expressStrategy,
        logisticsPackage,
        route,
        vehicle
    )
    validateStrategy(
        pricingEngine,
        "FragileStrategy",
        fragileStrategy,
        logisticsPackage,
        route,
        vehicle
    )
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
