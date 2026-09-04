package presentation

import data.dataholder.FleetRaw
import data.dataholder.LogisticsPackageRaw
import data.dataholder.RouteRaw
import data.dataholder.WarehouseRaw
import data.parser.FleetParser
import data.parser.PackageParser
import data.parser.RouteParser
import data.parser.WarehouseParser
import data.repository.PackageRepository
import data.repository.RouteRepository
import data.repository.VehicleRepository
import data.repository.WarehouseRepository
import data.repository.csv.CsvPackageRepository
import data.repository.csv.CsvRouteRepository
import data.repository.csv.CsvVehicleRepository
import data.repository.csv.CsvWarehouseRepository
import domain.algorithm.sorting.sortPackages
import domain.builder.DomainGraphBuilder
import domain.builder.GraphBuilderInput
import domain.model.Warehouse
import domain.model.request.AnalyzeTreePerformanceRequest
import domain.pricing.EcoStrategy
import domain.pricing.ExpressStrategy
import domain.pricing.FragileStrategy
import domain.pricing.RoutePricingEngine
import domain.pricing.decorator.PackageDecoratorComposer
import domain.routing.BidirectionalRouter
import domain.routing.LeastHopRouter
import domain.routing.ShortestPathRouter
import domain.tree.simulation.*
import domain.usecase.*
import domain.validation.*
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

    // Step 1: Configure repositories and connect them to their parsers.
    val packageRepository = createPackageRepository()
    val warehouseRepository = createWarehouseRepository()
    val routeRepository = createRouteRepository()
    val vehicleRepository = createVehicleRepository()

    // Step 2: Group repositories required by the domain graph builder.
    val graphRepositories = GraphBuilderInput(
        packageRepository = packageRepository,
        warehouseRepository = warehouseRepository,
        vehicleRepository = vehicleRepository,
        routeRepository = routeRepository
    )

    // Step 2: Load and preprocess raw data from CSV files.
    val rawPackages = loadPackages(packageRepository)
    sortPackagesByPriorityAndWeight(rawPackages)
    displayTopPriorityPackages(rawPackages)

    // Step 4: Build the domain graph.
    val warehouseIndex = buildProjectDomainGraph(repositories = graphRepositories)

    // Step 4: Validate graph construction and package sorting.
    validateGraphAndSorting(warehouseIndex)

    // Step 5: Validate pricing strategies and package decorators.
    validatePricingAndDecorators(warehouseIndex)

    // Step 6: Validate routing algorithms.
    validateRoutingAlgorithms(warehouseIndex)
    // Step 7:Validate HubTree
    HubTreeValidation(warehouseIndex).validateHubTree()

    val trackingIdGenerator = SequentialTrackingIdGenerator()

    val unbalancedBST = UnbalancedBST()

    val balancedBST = BalancedBST()

    val analyzeTreePerformanceUseCase = AnalyzeTreePerformanceUseCase(
        generator = trackingIdGenerator,
        unbalancedBST = unbalancedBST,
        balancedBST = balancedBST
    )
    val analyzeTreePerformanceRequest = AnalyzeTreePerformanceRequest(
        numberOfTrackingIds = 1000,
        nanoTimeProvider = System::nanoTime
    )
    val simulationResult = analyzeTreePerformanceUseCase(analyzeTreePerformanceRequest)

    displayTreePerformanceResult(simulationResult)

    // Step 8: Validate Command Pattern
    validateCommandPattern(warehouseIndex)

}

private fun createPackageRepository(): PackageRepository {
    val packageParser = PackageParser()
    return CsvPackageRepository(
        packageFile = File(PACKAGES_FILE_PATH),
        packageParser = packageParser
    )
}

private fun createWarehouseRepository(): WarehouseRepository {

    val warehouseParser = WarehouseParser()

    return CsvWarehouseRepository(
        warehouseFile = File(WAREHOUSES_FILE_PATH),
        warehouseParser = warehouseParser
    )
}

private fun createRouteRepository(): RouteRepository {

    val routeParser = RouteParser()

    return CsvRouteRepository(
        routeFile = File(ROUTES_FILE_PATH),
        routeParser = routeParser
    )
}

private fun createVehicleRepository(): VehicleRepository {

    val fleetParser = FleetParser()

    return CsvVehicleRepository(
        vehicleFile = File(FLEET_FILE_PATH),
        fleetParser = fleetParser
    )
}

private fun buildProjectDomainGraph(
    repositories: GraphBuilderInput
): Map<String, Warehouse> {

    val domainGraphBuilder = DomainGraphBuilder(repositories = repositories)

    return domainGraphBuilder.buildGraph()
}

private fun validateGraphAndSorting(
    warehouseIndex: Map<String, Warehouse>
) {
    validateDomainGraph(warehouseIndex)

    val sortCargoQueueUseCase = SortingCargoQueueUseCase()

    validateQuickSort(
        warehouseIndex = warehouseIndex,
        sortCargoQueueUseCase = sortCargoQueueUseCase
    )
}

private fun validatePricingAndDecorators(
    warehouseIndex: Map<String, Warehouse>
) {
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

    val packageDecoratorComposer = PackageDecoratorComposer()

    val calculatePricingUseCase =
        CalculatePricingUseCase(packageDecoratorComposer)

    validateDecoratorIntegration(
        warehouseIndex = warehouseIndex,
        calculatePricingUseCase = calculatePricingUseCase,
        dispatchStrategy = ecoStrategy
    )
}

private fun validateRoutingAlgorithms(
    warehouseIndex: Map<String, Warehouse>
) {
    val leastHopRouter = LeastHopRouter()

    val findFewestHopsRouteUseCase =
        FindFewestHopsRouteUseCase(leastHopRouter)

    val routingValidation = LeastHopRoutingValidation(
        warehouseIndex = warehouseIndex,
        findFewestHopsRouteUseCase = findFewestHopsRouteUseCase
    )
    routingValidation.validateRouting()


    val shortestPathRouter = ShortestPathRouter(
        warehouseIndex.values.toList()
    )

    val findOptimalPathUseCase = FindOptimalPathUseCase(
        shortestPathFinder = shortestPathRouter
    )

    val dijkstraRoutingValidation = DijkstraRoutingValidation(
        warehouseIndex = warehouseIndex,
        router = shortestPathRouter,
        findOptimalPathUseCase = findOptimalPathUseCase
    )
    dijkstraRoutingValidation.validateRouting()


    val bidirectionalRouter = BidirectionalRouter(
        warehouseIndex.values.toList()
    )

    val bidirectionalRoutingValidation = BidirectionalRoutingValidation(
        warehouseIndex = warehouseIndex,
        router = bidirectionalRouter
    )
    bidirectionalRoutingValidation.validateRouting()
}

private fun loadPackages(
    packageRepository: PackageRepository
): MutableList<LogisticsPackageRaw> {

    println("Starting Package Data Pipeline...")

    val packages = packageRepository
        .loadPackages()
        .toMutableList()

    println("Successfully loaded ${packages.size} " + "valid package records.")
    return packages
}

private fun sortPackagesByPriorityAndWeight(
    packages: MutableList<LogisticsPackageRaw>
) {
    sortPackages(packages)
}

private fun displayTopPriorityPackages(
    packages: List<LogisticsPackageRaw>
) {
    println()
    println("Top $TOP_PACKAGE_COUNT priority packages:")

    val displayedPackageCount = minOf(TOP_PACKAGE_COUNT, packages.size)

    for (packageIndex in packages.indices.take(displayedPackageCount)
    ) {
        val packagePosition = packageIndex + DISPLAY_POSITION_OFFSET

        val logisticsPackage = packages[packageIndex]

        println(
            "$packagePosition. " + "ID: ${logisticsPackage.packageId}" +
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
        "Successfully loaded ${warehouses.size} " + "valid warehouse records."
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
            "Route ID: ${route.routeId}" + " | Origin Hub: ${route.originHubId}" +
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

    println("Successfully loaded ${vehicles.size} " + "valid vehicle records.")
    println("--- Detailed Vehicle Records ---")

    for (vehicle in vehicles) {
        println(
            "Vehicle ID: ${vehicle.vehicleId}" + " | Current Hub: ${vehicle.currentHubId}" +
                    " | Max Capacity: ${vehicle.maxCapacityKg} kg" +
                    " | Cost Per Km: $${vehicle.costPerKm}"
        )
    }
    println("--- Vehicle Data Initialization Complete ---")
    printSeparator()

    return vehicles
}


private fun validateQuickSort(
    warehouseIndex: Map<String, Warehouse>,
    sortCargoQueueUseCase: SortingCargoQueueUseCase
) {
    println()
    println("=== QuickSort Validation ===")

    val firstWarehouse = warehouseIndex.values.firstOrNull()

    if (firstWarehouse == null) {
        println("QuickSort validation skipped: No warehouses were built.")
        printSeparator()
        return
    }

    println("Warehouse: ${firstWarehouse.id}")
    println()
    println("Cargo Queue Before Sorting:")
    printCargoQueue(firstWarehouse)


    warehouseIndex.values.forEach { warehouse ->
        sortCargoQueueUseCase(warehouse)
    }

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
        println(logisticsPackage.id + " | ${formatWeight(logisticsPackage.weightByKg)} kg")
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

private fun displayTreePerformanceResult(
    result: SimulationResult
) {
    println("===== TREE PERFORMANCE ANALYSIS =====")

    println()
    println(getTreeTypeDisplayName(result.unbalancedTreeMetrics.treeType))
    println("Steps: ${result.unbalancedTreeMetrics.searchSteps}")
    println("Time: ${result.unbalancedTreeMetrics.executionTimeNanos} ns")

    println()
    println(getTreeTypeDisplayName(result.balancedTreeMetrics.treeType))
    println("Steps: ${result.balancedTreeMetrics.searchSteps}")
    println("Time: ${result.balancedTreeMetrics.executionTimeNanos} ns")
}

private fun getTreeTypeDisplayName(
    treeType: TreeType
): String {
    return when (treeType) {
        TreeType.UNBALANCED_BST -> "Unbalanced BST"
        TreeType.BALANCED_BST -> "Balanced BST"
    }
}

