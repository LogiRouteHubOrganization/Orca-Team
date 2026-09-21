package presentation

import data.datasource.csv.CsvPackageDataSource
import data.datasource.csv.CsvRouteDataSource
import data.datasource.csv.CsvVehicleDataSource
import data.datasource.csv.CsvWarehouseDataSource
import data.exception.remote.NetworkException
import data.mapper.PackageMapper
import data.mapper.RouteMapper
import data.mapper.VehicleMapper
import data.mapper.WarehouseMapper
import data.parser.FleetParser
import data.parser.PackageParser
import data.parser.RouteParser
import data.parser.WarehouseParser
import data.remote.datasource.VehicleRemoteDataSource
import data.remote.dto.request.VehicleCreateRequestDto
import data.remote.dto.request.VehicleUpdateRequestDto
import data.remote.dto.response.VehicleDto
import data.remote.datasource.supabase.VehicleRemoteDataSourceImpl
import data.remote.datasource.supabase.WarehouseRemoteDataSourceImpl
import data.remote.datasource.supabase.RouteRemoteDataSourceImpl
import data.repository.local.LocalPackageRepositoryImpl
import data.repository.local.LocalRouteRepositoryImpl
import data.repository.local.LocalVehicleRepositoryImpl
import data.repository.local.LocalWarehouseRepositoryImpl
import data.repository.remote.RemoteRouteRepositoryImpl
import data.repository.remote.RemoteVehicleRepositoryImpl
import data.repository.remote.RemoteWarehouseRepositoryImpl
import data.validation.LocalDataSourceValidator
import domain.algorithm.dispatcher.GreedyFleetDispatcher
import domain.algorithm.dispatcher.VehicleCoverageMapper
import domain.exception.EntityConflictException
import domain.exception.DomainException
import domain.exception.EntityValidationException
import domain.exception.NetworkUnavailableException
import domain.exception.ResourceNotFoundException
import domain.exception.ServerErrorException
import domain.exception.UnauthorizedException
import domain.exception.UnknownDomainException
import domain.model.Warehouse
import domain.demo.*
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
import domain.usecase.crud.vehicle.GetVehicleByIdUseCase
import domain.usecase.crud.warehouse.CreateWarehouseUseCase
import java.io.File
import java.util.*

private const val PACKAGES_FILE_PATH = "src/main/resources/packages.csv"
private const val WAREHOUSES_FILE_PATH = "src/main/resources/warehouses.csv"
private const val ROUTES_FILE_PATH = "src/main/resources/routes.csv"
private const val FLEET_FILE_PATH = "src/main/resources/fleet.csv"

private const val WEIGHT_DECIMAL_FORMAT = "%.2f"
private const val OUTPUT_SEPARATOR =
    "******************************************************"

suspend fun main() {

    runCatching {
        runApplication()
    }.onFailure { exception ->
        println()
        println("===== APPLICATION ERROR =====")
        println(formatApplicationError(exception))
        println("The application completed without an unexpected crash.")
    }
}

private suspend fun runApplication() {
/*
    /*
     * -------------------------------------------------------------
     * PART 1: Existing local CSV flow
     * -------------------------------------------------------------
     */

    val localDataSourceValidator = LocalDataSourceValidator()

    val warehouseDataSource = CsvWarehouseDataSource(
        warehouseFile = File(WAREHOUSES_FILE_PATH),
        warehouseParser = WarehouseParser(),
        localDataSourceValidator = localDataSourceValidator
    )

    val packageDataSource = CsvPackageDataSource(
        packageFile = File(PACKAGES_FILE_PATH),
        packageParser = PackageParser(),
        localDataSourceValidator = localDataSourceValidator
    )

    val vehicleDataSource = CsvVehicleDataSource(
        vehicleFile = File(FLEET_FILE_PATH),
        fleetParser = FleetParser(),
        localDataSourceValidator = localDataSourceValidator
    )

    val routeDataSource = CsvRouteDataSource(
        routeFile = File(ROUTES_FILE_PATH),
        routeParser = RouteParser(),
        localDataSourceValidator = localDataSourceValidator
    )

    val warehouseMapper = WarehouseMapper()
    val packageMapper = PackageMapper()
    val vehicleMapper = VehicleMapper()
    val routeMapper = RouteMapper()

    val warehouseRepository = LocalWarehouseRepositoryImpl(
        warehouseDataSource = warehouseDataSource,
        warehouseMapper = warehouseMapper
    )

    val packageRepository = LocalPackageRepositoryImpl(
        packageDataSource = packageDataSource,
        packageMapper = packageMapper,
        warehouseRepository = warehouseRepository
    )

    val vehicleRepository = LocalVehicleRepositoryImpl(
        vehicleDataSource = vehicleDataSource,
        vehicleMapper = vehicleMapper,
        warehouseRepository = warehouseRepository
    )

    val routeRepository = LocalRouteRepositoryImpl(
        routeDataSource = routeDataSource,
        routeMapper = routeMapper,
        warehouseRepository = warehouseRepository
    )

    val warehouses = warehouseRepository.findAll().getOrThrow()

    packageRepository.findAll().getOrThrow()
    vehicleRepository.findAll().getOrThrow()
    routeRepository.findAll().getOrThrow()

    val warehouseIndex = warehouses.associateBy { it.id }

    validatePricingAndDecorators(warehouseIndex)

    validateRoutingAlgorithms(
        warehouseIndex = warehouseIndex,
        warehouseRepository = warehouseRepository
    )

    HubTreeDemo(warehouseIndex).runDemo()

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

*/
    /*
     * -------------------------------------------------------------
     * PART 2: Existing successful CRUD demonstration
     * -------------------------------------------------------------
     */
/*
    val createWarehouseUseCase = CreateWarehouseUseCase(warehouseRepository = warehouseRepository)

    val warehouse = Warehouse(
        id = "WH-000001",
        name = "Nablus Warehouse",
        regionalZone = "North",
        latitude = 32.2211,
        longitude = 35.2544
    )

    createWarehouseUseCase(warehouse)
        .onSuccess { createdWarehouse ->
            println()
            println("===== LOCAL CRUD SUCCESS =====")
            println(
                "Warehouse created successfully: " +
                        createdWarehouse.id
            )
        }
        .onFailure { exception ->
            println()
            println("===== LOCAL CRUD FAILURE =====")
            println(formatApplicationError(exception))
        }
*/
    /*
     * -------------------------------------------------------------
     * PART 3: Live Supabase repositories
     * -------------------------------------------------------------
     */

    val remoteWarehouseRepository =
        RemoteWarehouseRepositoryImpl(
            warehouseRemoteDataSource =
                WarehouseRemoteDataSourceImpl()
        )

    val remoteVehicleRepository =
        RemoteVehicleRepositoryImpl(
            vehicleRemoteDataSource =
                VehicleRemoteDataSourceImpl(),
            warehouseRepository =
                remoteWarehouseRepository
        )

    val remoteRouteRepository =
        RemoteRouteRepositoryImpl(
            routeRemoteDataSource =
                RouteRemoteDataSourceImpl(),
            warehouseRepository =
                remoteWarehouseRepository
        )

    /*
     * -------------------------------------------------------------
     * PART 4: Successful live CRUD operation
     * -------------------------------------------------------------
     */

    val liveVehicles =
        remoteVehicleRepository
            .findAll()
            .getOrThrow()

    println()
    println("===== LIVE SUPABASE CRUD =====")

    if (liveVehicles.isEmpty()) {
        println("No vehicles were returned from Supabase.")
    } else {
        val getVehicleByIdUseCase =
            GetVehicleByIdUseCase(
                vehicleRepository = remoteVehicleRepository
            )

        val existingVehicleId = liveVehicles.first().id

        getVehicleByIdUseCase(existingVehicleId)
            .onSuccess { vehicle ->
                if (vehicle == null) {
                    println(
                        "Vehicle $existingVehicleId was not found."
                    )
                } else {
                    println(
                        "Vehicle loaded successfully: ${vehicle.id}"
                    )
                }
            }
            .onFailure { exception ->
                println(formatApplicationError(exception))
            }
    }

    /*
     * -------------------------------------------------------------
     * PART 5: Deliberately invalid input
     * -------------------------------------------------------------
     */

    println()
    println("===== INVALID INPUT DEMONSTRATION =====")

    val getVehicleByIdUseCase =
        GetVehicleByIdUseCase(
            vehicleRepository = remoteVehicleRepository
        )

    getVehicleByIdUseCase("INVALID-ID")
        .onSuccess {
            println("Unexpected success for invalid vehicle ID.")
        }
        .onFailure { exception ->
            println(formatApplicationError(exception))
        }

    /*
     * -------------------------------------------------------------
     * PART 6: Deliberately not-found resource
     * -------------------------------------------------------------
     */

    println()
    println("===== NOT FOUND DEMONSTRATION =====")

    getVehicleByIdUseCase("TRK-999999")
        .onSuccess { vehicle ->
            if (vehicle == null) {
                println(
                    "Vehicle TRK-999999 was not found."
                )
            } else {
                println(
                    "Vehicle found unexpectedly: ${vehicle.id}"
                )
            }
        }
        .onFailure { exception ->
            println(formatApplicationError(exception))
        }

    /*
     * -------------------------------------------------------------
     * PART 7: Greedy Fleet Dispatcher
     * -------------------------------------------------------------
     */

    println()
    println("===== GREEDY FLEET DISPATCH =====")

    val greedyInputUseCase =
        GetGreedyDispatchDataUseCase(
            vehicleRepository = remoteVehicleRepository,
            routeRepository = remoteRouteRepository,
            greedyInputPreparer = VehicleCoverageMapper()
        )

    greedyInputUseCase()
        .onSuccess { vehicleCoverages ->

            val targetZones = vehicleCoverages
                .flatMap { coverage ->
                    coverage.coveredZones
                }
                .toSet()

            if (targetZones.isEmpty()) {
                println(
                    "Greedy dispatch skipped: " +
                            "no reachable delivery zones were found."
                )
            } else {

                val dispatcher = GreedyFleetDispatcher()

                val dispatchResult = dispatcher.dispatch(
                    targetZones = targetZones,
                    vehicleCoverages = vehicleCoverages
                )

                println(
                    "Target zones: ${targetZones.sorted()}"
                )

                println("Selected vehicles:")

                if (dispatchResult.selectedVehicles.isEmpty()) {
                    println("No vehicle was selected.")
                } else {
                    dispatchResult.selectedVehicles.forEach { vehicle ->
                        println(
                            "- ${vehicle.id} " +
                                    "(warehouse=${vehicle.currentWarehouse.id})"
                        )
                    }
                }

                println(
                    "Uncovered zones: " +
                            dispatchResult.uncoveredZones.sorted()
                )
            }
        }
        .onFailure { exception ->
            println(formatApplicationError(exception))
        }

    /*
     * -------------------------------------------------------------
     * PART 8: Deliberate network/API failure
     * -------------------------------------------------------------
     */

    demonstrateNetworkFailure()
}

/*
 * -------------------------------------------------------------
 * Application-level error formatting
 * -------------------------------------------------------------
 */

private fun formatApplicationError(
    exception: Throwable
): String {

    return when (exception) {

        is EntityValidationException ->
            "Invalid input: ${exception.message}"

        is ResourceNotFoundException ->
            "Resource not found: ${exception.message}"

        is NetworkUnavailableException ->
            "Network/API error: ${exception.message}"

        is EntityConflictException ->
            "Database conflict: ${exception.message}"

        is UnauthorizedException ->
            "Authorization error: ${exception.message}"

        is ServerErrorException ->
            "Server error: ${exception.message}"

        is UnknownDomainException ->
            "Unexpected data error: ${exception.message}"

        is DomainException ->
            "Domain error: ${exception.message}"

        else ->
            "Unexpected application error: " +
                    (exception.message
                        ?: exception::class.simpleName)
    }
}

/*
 * Demonstrates how a low-level network failure becomes
 * NetworkUnavailableException and is handled at the application
 * boundary without a try/catch block.
 */
private suspend fun demonstrateNetworkFailure() {

    println()
    println("===== NETWORK/API FAILURE DEMONSTRATION =====")

    val failingDataSource =
        FailingVehicleRemoteDataSource()

    val repository =
        RemoteVehicleRepositoryImpl(
            vehicleRemoteDataSource = failingDataSource,
            warehouseRepository =
                RemoteWarehouseRepositoryImpl(
                    WarehouseRemoteDataSourceImpl()
                )
        )

    val getVehicleByIdUseCase =
        GetVehicleByIdUseCase(
            vehicleRepository = repository
        )

    getVehicleByIdUseCase("TRK-000001")
        .onSuccess {
            println("Unexpected network success.")
        }
        .onFailure { exception ->
            println(formatApplicationError(exception))
        }
}

/*
 * Test double used only to demonstrate the error architecture.
 * It simulates a network outage at the data-source boundary.
 */
private class FailingVehicleRemoteDataSource :
    VehicleRemoteDataSource {

    override suspend fun getAllVehicles(): List<VehicleDto> {
        throw NetworkException.NoInternetException(
            "Simulated network failure for ST4 verification"
        )
    }

    override suspend fun getVehicleById(
        id: String
    ): VehicleDto? {
        throw NetworkException.NoInternetException(
            "Simulated network failure for ST4 verification"
        )
    }

    override suspend fun createVehicle(
        vehicleRequest: VehicleCreateRequestDto
    ): VehicleDto {
        throw NetworkException.NoInternetException(
            "Simulated network failure for ST4 verification"
        )
    }

    override suspend fun updateVehicle(
        id: String,
        vehicleRequest: VehicleUpdateRequestDto
    ): VehicleDto {
        throw NetworkException.NoInternetException(
            "Simulated network failure for ST4 verification"
        )
    }

    override suspend fun deleteVehicle(
        id: String
    ) {
        throw NetworkException.NoInternetException(
            "Simulated network failure for ST4 verification"
        )
    }
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

    val calculatePricingUseCase = CalculatePricingUseCase(packageDecoratorComposer)

    demonstrateDecoratorIntegration(
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

    val leastHopRoutingDemo = LeastHopRoutingDemo(
            warehouseIndex = warehouseIndex,
            findFewestHopsRouteUseCase =
                findFewestHopsRouteUseCase
        )

    leastHopRoutingDemo.runDemo()

    val shortestPathRouter = ShortestPathRouter(
            warehouseIndex.values.toList()
        )

    val findOptimalPathUseCase = FindOptimalPathUseCase(
            warehouseRepository = warehouseRepository,
            shortestPathFinder = shortestPathRouter
        )

    val dijkstraRoutingDemo =
        DijkstraRoutingDemo(
            warehouseIndex = warehouseIndex,
            findOptimalPathUseCase =
                findOptimalPathUseCase
        )

    dijkstraRoutingDemo.runDemo()

    val bidirectionalRouter = BidirectionalRouter(
            warehouseIndex.values.toList()
        )

    val bidirectionalRoutingDemo = BidirectionalRoutingDemo(
            warehouseIndex = warehouseIndex,
            router = bidirectionalRouter
        )

    bidirectionalRoutingDemo.runDemo()
}

private fun validateQuickSort(
    warehouseIndex: Map<String, Warehouse>,
    sortCargoQueueUseCase: SortingCargoQueueUseCase
) {
    println()
    println("=== QuickSort Validation ===")

    val firstWarehouse =
        warehouseIndex.values.firstOrNull()

    if (firstWarehouse == null) {
        println(
            "QuickSort validation skipped: " +
                    "No warehouses were built."
        )
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
        println(
            "No packages are available in this warehouse."
        )
        return
    }

    for (logisticsPackage in warehouse.cargoQueue) {
        println(
            logisticsPackage.id +
                    " | " +
                    "${formatWeight(logisticsPackage.weightByKg)} kg"
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
        "Steps: " +
                result.unbalancedTreeMetrics.searchSteps
    )
    println(
        "Time: " +
                result.unbalancedTreeMetrics.executionTimeNanos +
                " ns"
    )

    println()
    println(
        getTreeTypeDisplayName(
            result.balancedTreeMetrics.treeType
        )
    )
    println(
        "Steps: " +
                result.balancedTreeMetrics.searchSteps
    )
    println(
        "Time: " +
                result.balancedTreeMetrics.executionTimeNanos +
                " ns"
    )
}

private fun getTreeTypeDisplayName(
    treeType: TreeType
): String {
    return when (treeType) {
        TreeType.UNBALANCED_BST ->
            "Unbalanced BST"

        TreeType.BALANCED_BST ->
            "Balanced BST"
    }
}
