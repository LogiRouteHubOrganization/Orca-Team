package presentation

import data.remote.datasource.PackageRemoteDataSourceImpl
import data.remote.datasource.RouteRemoteDataSourceImpl
import data.remote.datasource.VehicleRemoteDataSourceImpl
import data.remote.datasource.WarehouseRemoteDataSourceImpl
import data.remote.repository.PackageRemoteRepositoryImpl
import data.remote.repository.RouteRemoteRepositoryImpl
import data.remote.repository.VehicleRemoteRepositoryImpl
import data.remote.repository.WarehouseRemoteRepositoryImpl
import domain.model.Warehouse
import domain.repository.WarehouseRepository
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
import kotlinx.coroutines.runBlocking
import java.util.Locale

private const val WEIGHT_DECIMAL_FORMAT = "%.2f"
private const val OUTPUT_SEPARATOR = "******************************************************"

fun main() = runBlocking {

    println("==============================================")
    println("       LogiRouteHub - Supabase Live Mode")
    println("==============================================")
    println()

    try {

        // Step 1: Initialize Supabase Remote DataSources
        val warehouseDataSource = WarehouseRemoteDataSourceImpl()
        val packageDataSource = PackageRemoteDataSourceImpl()
        val vehicleDataSource = VehicleRemoteDataSourceImpl()
        val routeDataSource = RouteRemoteDataSourceImpl()

        // Step 2: Initialize Remote Repositories
        val warehouseRepository: WarehouseRepository =
            WarehouseRemoteRepositoryImpl(
                remoteDataSource = warehouseDataSource
            )

        val packageRepository =
            PackageRemoteRepositoryImpl(
                remoteDataSource = packageDataSource,
                warehouseRepository = warehouseRepository
            )

        val vehicleRepository =
            VehicleRemoteRepositoryImpl(
                remoteDataSource = vehicleDataSource,
                warehouseRepository = warehouseRepository
            )

        val routeRepository =
            RouteRemoteRepositoryImpl(
                remoteDataSource = routeDataSource,
                warehouseRepository = warehouseRepository
            )

        println("Loading live Supabase data...")

        // Step 3: Load live records
        val warehouses = warehouseRepository.findAll()
        val packages = packageRepository.findAll()
        val vehicles = vehicleRepository.findAll()
        val routes = routeRepository.findAll()

        println("Warehouses: ${warehouses.size}")
        println("Packages:   ${packages.size}")
        println("Vehicles:   ${vehicles.size}")
        println("Routes:     ${routes.size}")
        println()

        // Step 4: Build warehouse index
        val warehouseIndex = warehouses.associateBy { it.id }

        // Step 5: Validate existing algorithms
        validateSorting(warehouseIndex)

        validatePricingAndDecorators(warehouseIndex)

        validateRoutingAlgorithms(
            warehouseIndex = warehouseIndex,
            warehouseRepository = warehouseRepository
        )

        // Step 6: Validate HubTree
        HubTreeValidation(warehouseIndex).validateHubTree()

        val trackingIdGenerator = SequentialTrackingIdGenerator()
        val unbalancedBST = UnbalancedBST()
        val balancedBST = BalancedBST()

        val analyzeTreePerformanceUseCase =
            AnalyzeTreePerformanceUseCase(
                generator = trackingIdGenerator,
                unbalancedBST = unbalancedBST,
                balancedBST = balancedBST
            )

        val analyzeTreePerformanceRequest =
            AnalyzeTreePerformanceRequest(
                numberOfTrackingIds = 1000,
                nanoTimeProvider = System::nanoTime
            )

        val simulationResult =
            analyzeTreePerformanceUseCase(
                analyzeTreePerformanceRequest
            )

        displayTreePerformanceResult(simulationResult)

        // Step 7: Validate Command Pattern
        validateCommandPattern(warehouseIndex)
        validateCommandHistory(warehouseIndex)

        println()
        println("==============================================")
        println("Supabase live-data verification completed.")
        println("==============================================")

    } catch (e: Exception) {
        println()
        println("==============================================")
        println("ERROR")
        println("==============================================")
        println(e.message ?: "Unexpected error")
        println()
        e.printStackTrace()
    }
}

private fun validateSorting(
    warehouseIndex: Map<String, Warehouse>
) {
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

private suspend fun validateRoutingAlgorithms(
    warehouseIndex: Map<String, Warehouse>,
    warehouseRepository: WarehouseRepository
) {
    val leastHopRouter = LeastHopRouter()

    val findFewestHopsRouteUseCase = FindFewestHopsRouteUseCase(
        warehouseRepository = warehouseRepository,
        pathFinder = leastHopRouter
    )

    val routingValidation = LeastHopRoutingValidation(
        warehouseIndex = warehouseIndex,
        findFewestHopsRouteUseCase = findFewestHopsRouteUseCase
    )

    routingValidation.validateRouting()

    val shortestPathRouter = ShortestPathRouter(
        warehouseIndex.values.toList()
    )

    val findOptimalPathUseCase = FindOptimalPathUseCase(
        warehouseRepository = warehouseRepository,
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

    val bidirectionalRoutingValidation =
        BidirectionalRoutingValidation(
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
        println("QuickSort validation skipped: No warehouses were loaded.")
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
        println(
            logisticsPackage.id +
                    " | " +
                    formatWeight(logisticsPackage.weightByKg) +
                    " kg"
        )
    }
}

private fun formatWeight(
    weightByKg: Double
): String {
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
    println(
        getTreeTypeDisplayName(
            result.unbalancedTreeMetrics.treeType
        )
    )
    println(
        "Steps: ${result.unbalancedTreeMetrics.searchSteps}"
    )
    println(
        "Time: ${result.unbalancedTreeMetrics.executionTimeNanos} ns"
    )

    println()
    println(
        getTreeTypeDisplayName(
            result.balancedTreeMetrics.treeType
        )
    )
    println(
        "Steps: ${result.balancedTreeMetrics.searchSteps}"
    )
    println(
        "Time: ${result.balancedTreeMetrics.executionTimeNanos} ns"
    )
}

private fun getTreeTypeDisplayName(
    treeType: TreeType
): String {
    return when (treeType) {
        TreeType.UNBALANCED_BST -> "Unbalanced BST"
        TreeType.BALANCED_BST -> "Balanced BST"
    }
}