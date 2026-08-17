import algorithm.PackageQuickSorter
import algorithm.sortPackages
import dataHolder.FleetRaw
import dataHolder.LogisticsPackageRaw
import dataHolder.RouteRaw
import dataHolder.WarehouseRaw
import domain.builder.DomainGraphBuilder
import domain.builder.GraphBuilderInput
import domain.model.Warehouse
import domain.pricing.EcoStrategy
import domain.pricing.ExpressStrategy
import domain.pricing.FragileStrategy
import domain.pricing.RoutePricingEngine
import domain.pricing.decorator.PackageDecoratorComposer
import domain.routing.LeastHopRouter
import domain.routing.ShortestPathRouter
import parser.FleetParser
import parser.PackageParser
import parser.RouteParser
import parser.WarehouseParser
import repository.PackageRepository
import repository.RouteRepository
import repository.VehicleRepository
import repository.WarehouseRepository
import repository.csv.CsvPackageRepository
import repository.csv.CsvRouteRepository
import repository.csv.CsvVehicleRepository
import repository.csv.CsvWarehouseRepository
import validation.*
import java.io.File
import java.util.*

private const val PACKAGES_FILE_PATH = "src/main/resources/packages.csv"
private const val WAREHOUSES_FILE_PATH = "src/main/resources/warehouses.csv"
private const val ROUTES_FILE_PATH = "src/main/resources/routes.csv"
private const val FLEET_FILE_PATH = "src/main/resources/fleet.csv"

private const val TOP_PACKAGE_COUNT = 3
private const val DISPLAY_POSITION_OFFSET = 1
private const val WEIGHT_DECIMAL_FORMAT = "%.2f"

private const val OUTPUT_SEPARATOR = "******************************************************"

fun main() {

    // Step 1: Configure parsers and repositories.
    val packageParser = PackageParser()
    val packageRepository: PackageRepository = CsvPackageRepository(
        packageFile = File(PACKAGES_FILE_PATH),
        packageParser = packageParser
    )

    val warehouseParser = WarehouseParser()

    val warehouseRepository: WarehouseRepository = CsvWarehouseRepository(
        warehouseFile = File(WAREHOUSES_FILE_PATH),
        warehouseParser = warehouseParser
    )

    val routeParser = RouteParser()

    val routeRepository: RouteRepository = CsvRouteRepository(
        routeFile = File(ROUTES_FILE_PATH),
        routeParser = routeParser
    )
    val fleetParser = FleetParser()

    val vehicleRepository: VehicleRepository = CsvVehicleRepository(
        vehicleFile = File(FLEET_FILE_PATH),
        fleetParser = fleetParser
    )
    // Step 2: Load raw data from CSV files.
    val rawPackages = loadPackages(packageRepository)
    sortAndDisplayPackages(rawPackages)

    val rawWarehouses = loadWarehouses(warehouseRepository)

    val rawRoutes = loadRoutes(routeRepository)

    val rawVehicles = loadVehicles(vehicleRepository)

    // Step 3: Build the domain graph.

    val domainGraphBuilder = DomainGraphBuilder()
    val warehouseIndex = buildDomainGraph(
        rawPackages = rawPackages,
        rawWarehouses = rawWarehouses,
        rawRoutes = rawRoutes,
        rawVehicles = rawVehicles,
        domainGraphBuilder = domainGraphBuilder
    )

    // Step 4: Validate graph construction and sorting.

    validateDomainGraph(warehouseIndex)

    val packageQuickSorter = PackageQuickSorter()
    validateQuickSort(
        warehouseIndex = warehouseIndex,
        packageQuickSorter = packageQuickSorter
    )
    // Step 5: Validate pricing strategies and decorators.

    val ecoStrategy = EcoStrategy()

    val expressStrategy = ExpressStrategy()

    val fragileStrategy = FragileStrategy()

    val pricingEngine = RoutePricingEngine(ecoStrategy)
    validatePricingStrategies(
        warehousesById = warehouseIndex,
        pricingEngine = pricingEngine,
        ecoStrategy = ecoStrategy,
        expressStrategy = expressStrategy,
        fragileStrategy = fragileStrategy
    )

    val dispatchStrategy = EcoStrategy()
    val decoratorComposer = PackageDecoratorComposer()
    validateDecoratorIntegration(
        warehouseIndex = warehouseIndex,
        dispatchStrategy = dispatchStrategy,
        decoratorComposer = decoratorComposer
    )

    // Step 6: Validate routing algorithms.

    val leastHopRouter = LeastHopRouter()

    val routingValidation = LeastHopRoutingValidation(
        warehouseIndex = warehouseIndex,
        router = leastHopRouter
    )
    routingValidation.validateRouting()

    val shortestPathRouter = ShortestPathRouter(
        warehouseIndex.values.toList()
    )

    val dijkstraRoutingValidation = DijkstraRoutingValidation(
        warehouseIndex = warehouseIndex,
        router = shortestPathRouter
    )

    dijkstraRoutingValidation.validateRouting()

}

private fun loadPackages(
    packageRepository: PackageRepository
): MutableList<LogisticsPackageRaw> {

    println("Starting Package Data Pipeline...")

    val packages =
        packageRepository
            .loadPackages()
            .toMutableList()

    println(
        "Successfully loaded ${packages.size} " +
                "valid package records."
    )

    return packages
}

private fun sortAndDisplayPackages(
    packages: MutableList<LogisticsPackageRaw>
) {

    sortPackages(packages)

    println()
    println("Top $TOP_PACKAGE_COUNT priority packages:")

    val displayedPackageCount = minOf(
        TOP_PACKAGE_COUNT,
        packages.size
    )

    for (packageIndex in packages.indices.take(
        displayedPackageCount
    )) {
        val packagePosition =
            packageIndex + DISPLAY_POSITION_OFFSET

        val logisticsPackage =
            packages[packageIndex]

        println(
            "$packagePosition. " +
                    "ID: ${logisticsPackage.packageId}" +
                    " | Weight: " +
                    "${formatWeight(logisticsPackage.packageWeightByKg)} kg" +
                    " | Priority: ${logisticsPackage.packagePriority}"
        )
    }
    printSeparator()
}

private fun loadWarehouses(
    warehouseRepository: WarehouseRepository
): List<WarehouseRaw> {

    println("Starting Warehouse Data Pipeline...")

    val warehouses = warehouseRepository.loadWarehouses()

    println(
        "Successfully loaded ${warehouses.size} " +
                "valid warehouse records."
    )

    println("--- Detailed Warehouse Records ---")

    for (warehouse in warehouses) {
        println(
            "ID: ${warehouse.warehouseId}" +
                    " | Name: ${warehouse.warehouseName}" +
                    " | Zone: ${warehouse.regionalZone}" +
                    " | Latitude: ${warehouse.latitude}" +
                    " | Longitude: ${warehouse.longitude}"
        )
    }

    println("--- Warehouse Data Initialization Complete ---")
    printSeparator()

    return warehouses
}

private fun loadRoutes(
    routeRepository: RouteRepository
): List<RouteRaw> {

    println("Starting Route Data Pipeline...")

    val routes = routeRepository.loadRoutes()

    println("Successfully loaded ${routes.size} " + "valid route records.")

    println("--- Detailed Route Records ---")

    for (route in routes) {
        println(
            "Route ID: ${route.routeId}" +
                    " | Origin Hub: ${route.originHubId}" +
                    " | Destination Hub: ${route.destinationHubId}" +
                    " | Distance: ${route.distanceKm} km" +
                    " | Typical Delay: ${route.typicalDelayMin} min"
        )
    }

    println("--- Route Data Initialization Complete ---")

    printSeparator()

    return routes
}

private fun loadVehicles(
    vehicleRepository: VehicleRepository
): List<FleetRaw> {

    println("Starting Vehicle Data Pipeline...")

    val vehicles = vehicleRepository.loadVehicles()

    println(
        "Successfully loaded ${vehicles.size} " +
                "valid vehicle records."
    )

    println("--- Detailed Vehicle Records ---")

    for (vehicle in vehicles) {
        println(
            "Vehicle ID: ${vehicle.vehicleId}" +
                    " | Current Hub: ${vehicle.currentHubId}" +
                    " | Max Capacity: ${vehicle.maxCapacityKg} kg" +
                    " | Cost Per Km: $${vehicle.costPerKm}"
        )
    }

    println("--- Vehicle Data Initialization Complete ---")
    printSeparator()

    return vehicles
}

private fun buildDomainGraph(
    rawPackages: List<LogisticsPackageRaw>,
    rawWarehouses: List<WarehouseRaw>,
    rawRoutes: List<RouteRaw>,
    rawVehicles: List<FleetRaw>,
    domainGraphBuilder: DomainGraphBuilder
): Map<String, Warehouse> {

    val graphBuilderInput = GraphBuilderInput(
        warehouses = rawWarehouses,
        packages = rawPackages,
        vehicles = rawVehicles,
        routes = rawRoutes
    )

    return domainGraphBuilder.buildGraph(
        graphBuilderInput
    )
}

private fun validateQuickSort(
    warehouseIndex: Map<String, Warehouse>,
    packageQuickSorter: PackageQuickSorter
) {

    println()
    println("=== QuickSort Validation ===")

    val firstWarehouse =
        warehouseIndex.values.firstOrNull()

    if (firstWarehouse == null) {
        println("QuickSort validation skipped: " + "No warehouses were built.")
        printSeparator()
        return
    }

    println("Warehouse: ${firstWarehouse.id}")
    println()

    println("Cargo Queue Before Sorting:")

    printCargoQueue(firstWarehouse)

    packageQuickSorter.sortAllCargoQueuesByDescendingWeight(warehouseIndex)

    println()

    println("Cargo Queue After Sorting:")

    printCargoQueue(firstWarehouse)

    printSeparator()
}

private fun printCargoQueue(
    warehouse: Warehouse
) {
    if (warehouse.cargoQueue.isEmpty()) {
        println("No packages are available in this warehouse.")
        return
    }

    for (logisticsPackage in warehouse.cargoQueue) {
        println(
            logisticsPackage.id +
                    " | ${formatWeight(logisticsPackage.weightByKg)} kg"
        )
    }
}

private fun formatWeight(weightByKg: Double): String {
    return String.format(
        Locale.US,
        WEIGHT_DECIMAL_FORMAT,
        weightByKg
    )
}

private fun printSeparator() {
    println()
    println(OUTPUT_SEPARATOR)
    println()
}