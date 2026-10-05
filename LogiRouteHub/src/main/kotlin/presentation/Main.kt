package presentation

import di.networkModule
import di.repositoryModule
import di.useCaseModule
import di.validatorModule
import domain.repository.PackageRepository
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import domain.usecase.AnalyzePackageDestinationDemandUseCase
import domain.usecase.FindFewestHopsRouteUseCase
import domain.usecase.FindOptimalPathUseCase
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.context.startKoin

fun main(): Unit = runBlocking {

    val koin = startKoin {
        modules(
            networkModule,
            repositoryModule,
            validatorModule,
            useCaseModule
        )
    }.koin

    Main(koin).run()
}

class Main(
    private val koin: Koin
) {

    private val warehouseRepository = koin.get<WarehouseRepository>()

    private val packageRepository = koin.get<PackageRepository>()

    private val vehicleRepository = koin.get<VehicleRepository>()

    private val routeRepository = koin.get<RouteRepository>()

    private val findOptimalPathUseCase = koin.get<FindOptimalPathUseCase>()

    private val findFewestHopsRouteUseCase = koin.get<FindFewestHopsRouteUseCase>()

    private val analyzePackageDestinationDemandUseCase =
        koin.get<AnalyzePackageDestinationDemandUseCase>()


    suspend fun run() {
        refreshData()
        initializeRelationships()
        demoRepositories()
        demoAlgorithms()
    }


    private suspend fun refreshData() {
        println("\n=== Refreshing Data ===")

        warehouseRepository.refresh()
            .onSuccess { println("Warehouses refreshed successfully.") }
            .onFailure { println("Warehouses refresh failed: ${it.message}") }

        packageRepository.refresh()
            .onSuccess { println("Packages refreshed successfully.") }
            .onFailure { println("Packages refresh failed: ${it.message}") }

        vehicleRepository.refresh()
            .onSuccess { println("Vehicles refreshed successfully.") }
            .onFailure { println("Vehicles refresh failed: ${it.message}") }

        routeRepository.refresh()
            .onSuccess { println("Routes refreshed successfully.") }
            .onFailure { println("Routes refresh failed: ${it.message}") }
    }


    private suspend fun initializeRelationships() {
        println("\n=== Initializing Relationships ===")

        packageRepository.initializeCargoQueues()
            .onSuccess { println("Cargo queues initialized.") }
            .onFailure { println("Cargo queues failed: ${it.message}") }

        vehicleRepository.initializeStationedVehicles()
            .onSuccess { println("Stationed vehicles initialized.") }
            .onFailure { println("Stationed vehicles failed: ${it.message}") }

        routeRepository.initializeOutgoingRoutes()
            .onSuccess { println("Outgoing routes initialized.") }
            .onFailure { println("Outgoing routes failed: ${it.message}") }
    }


    private suspend fun demoRepositories() {
        printWarehouses()
        printPackages()
        printVehicles()
        printRoutes()
    }


    private suspend fun printWarehouses() {
        println("\n=== Warehouses ===")

        warehouseRepository.findAll().onSuccess { warehouses ->
                println("Count: ${warehouses.size}")

                warehouses.take(5).forEach { warehouse ->
                    println(
                        "${warehouse.id}, ${warehouse.name}, " +
                                "${warehouse.regionalZone}, " +
                                "${warehouse.latitude}, ${warehouse.longitude}"
                    )
                }
            }
            .onFailure {
                println("Failed: ${it.message}")
            }
    }

    private suspend fun printPackages() {
        println("\n=== Packages ===")

        packageRepository.findAll().onSuccess { packages ->
                println("Count: ${packages.size}")

                packages.take(5).forEach { logisticsPackage ->
                    println(
                        "${logisticsPackage.id}, " +
                                "${logisticsPackage.weightByKg} kg, " +
                                "${logisticsPackage.priority}, " +
                                "${logisticsPackage.originWarehouse.id} -> " +
                                logisticsPackage.destinationWarehouse.id
                    )
                }
            }
            .onFailure {
                println("Failed: ${it.message}")
            }
    }


    private suspend fun printVehicles() {
        println("\n=== Vehicles ===")

        vehicleRepository.findAll()
            .onSuccess { vehicles -> println("Count: ${vehicles.size}")

                vehicles.take(5).forEach { vehicle ->
                    println(
                        "${vehicle.id}, " +
                                "${vehicle.maxCapacityKg} kg, " +
                                "${vehicle.costPerKm}/km, " +
                                "Warehouse: ${vehicle.currentWarehouse.id}"
                    )
                }
            }
            .onFailure {
                println("Failed: ${it.message}")
            }
    }

    private suspend fun printRoutes() {
        println("\n=== Routes ===")

        routeRepository.findAll().onSuccess { routes ->
                println("Count: ${routes.size}")

                routes.take(5).forEach { route ->
                    println(
                        "${route.id}, " +
                                "${route.originWarehouse.id} -> " +
                                "${route.destinationWarehouse.id}, " +
                                "${route.distanceKm} km, " +
                                "${route.typicalDelayMin} min"
                    )
                }
            }
            .onFailure {
                println("Failed: ${it.message}")
            }
    }

    private suspend fun demoAlgorithms() {
        val packages = packageRepository.findAll().getOrThrow()

        if (packages.isEmpty()) {
            println("\nNo packages available for algorithm testing.")
            return
        }

        val testPackage = packages.first()

        demoOptimalPath(testPackage.originWarehouse.id, testPackage.destinationWarehouse.id)

        demoFewestHops(testPackage.originWarehouse.id, testPackage.destinationWarehouse.id)

        demoDestinationDemand()
    }


    private suspend fun demoOptimalPath(
        startWarehouseId: String,
        destinationWarehouseId: String
    ) {
        println("\n=== Optimal Path ===")

        findOptimalPathUseCase(
            startWarehouseId = startWarehouseId,
            destinationWarehouseId = destinationWarehouseId
        )
            .onSuccess { path ->
                println(
                    "Path: " +
                            path.joinToString(" -> ") { warehouse ->
                                warehouse.id
                            }
                )
            }
            .onFailure {
                println("Failed: ${it.message}")
            }
    }


    private suspend fun demoFewestHops(
        startWarehouseId: String,
        destinationWarehouseId: String
    ) {
        println("\n=== Fewest Hops ===")

        findFewestHopsRouteUseCase(
            startWarehouseId = startWarehouseId,
            destinationWarehouseId = destinationWarehouseId
        )
            .onSuccess { path ->
                println(
                    "Path: " +
                            path.joinToString(" -> ") { warehouse ->
                                warehouse.id
                            }
                )
            }
            .onFailure {
                println("Failed: ${it.message}")
            }
    }


    private suspend fun demoDestinationDemand() {
        println("\n=== Package Destination Demand ===")

        val packages = packageRepository.findAll().getOrThrow()

        val warehouse = analyzePackageDestinationDemandUseCase(packages)

        if (warehouse != null) {
            println(
                "Most demanded destination: " +
                        "${warehouse.id}, ${warehouse.name}"
            )
        } else {
            println("No package demand found.")
        }
    }
}
