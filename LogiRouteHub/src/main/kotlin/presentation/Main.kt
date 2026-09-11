package presentation

import data.datasource.csv.CsvPackageDataSource
import data.datasource.csv.CsvRouteDataSource
import data.datasource.csv.CsvVehicleDataSource
import data.datasource.csv.CsvWarehouseDataSource
import data.mapper.PackageMapper
import data.mapper.RouteMapper
import data.mapper.VehicleMapper
import data.mapper.WarehouseMapper
import data.parser.FleetParser
import data.parser.PackageParser
import data.parser.RouteParser
import data.parser.WarehouseParser
import data.repository.PackageRepositoryImpl
import data.repository.RouteRepositoryImpl
import data.repository.VehicleRepositoryImpl
import data.repository.WarehouseRepositoryImpl
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

private const val WEIGHT_DECIMAL_FORMAT = "%.2f"
private const val OUTPUT_SEPARATOR = "******************************************************"

fun main() {

    // Step 1: Initialize DataSources
    val warehouseDataSource = CsvWarehouseDataSource(
        warehouseFile = File(WAREHOUSES_FILE_PATH),
        warehouseParser = WarehouseParser()
    )

    val packageDataSource = CsvPackageDataSource(
        packageFile = File(PACKAGES_FILE_PATH),
        packageParser = PackageParser()
    )

    val vehicleDataSource = CsvVehicleDataSource(
        vehicleFile = File(FLEET_FILE_PATH),
        fleetParser = FleetParser()
    )

    val routeDataSource = CsvRouteDataSource(
        routeFile = File(ROUTES_FILE_PATH),
        routeParser = RouteParser()
    )

    // Step 2: Initialize Mappers
    val warehouseMapper = WarehouseMapper()
    val packageMapper = PackageMapper()
    val vehicleMapper = VehicleMapper()
    val routeMapper = RouteMapper()

    // Step 3: Initialize Repositories
    val warehouseRepository = WarehouseRepositoryImpl(
        warehouseDataSource = warehouseDataSource,
        warehouseMapper = warehouseMapper
    )

    val packageRepository = PackageRepositoryImpl(
        packageDataSource = packageDataSource,
        packageMapper = packageMapper,
        warehouseRepository = warehouseRepository
    )

    val vehicleRepository = VehicleRepositoryImpl(
        vehicleDataSource = vehicleDataSource,
        vehicleMapper = vehicleMapper,
        warehouseRepository = warehouseRepository
    )

    val routeRepository = RouteRepositoryImpl(
        routeDataSource = routeDataSource,
        routeMapper = routeMapper,
        warehouseRepository = warehouseRepository
    )

    // Step 4: Load repository-backed domain state
    val warehouses = warehouseRepository.findAll()
    packageRepository.findAll()
    vehicleRepository.findAll()
    routeRepository.findAll()

    // Step 5: Build warehouse index for validations and algorithms
    val warehouseIndex = warehouses.associateBy {
        it.id
    }

    // Step 6: Validate graph construction and package sorting
    validateGraphAndSorting(warehouseIndex)

    // Step 7: Validate pricing strategies and package decorators
    validatePricingAndDecorators(warehouseIndex)

    // Step 8: Validate routing algorithms
    validateRoutingAlgorithms(warehouseIndex)

    // Step 9: Validate HubTree
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
    val simulationResult = analyzeTreePerformanceUseCase(
        analyzeTreePerformanceRequest
    )
    displayTreePerformanceResult(simulationResult)

    // Step 10: Validate Command Pattern
    validateCommandPattern(warehouseIndex)
    validateCommandHistory(warehouseIndex)
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

