package presentation

import di.dispatcherModule
import di.networkModule
import di.presentationModule
import di.repositoryModule
import di.telemetryModule
import di.useCaseModule
import di.validatorModule


import domain.repository.PackageRepository
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

import domain.usecase.AnalyzePackageDestinationDemandUseCase
import domain.usecase.FindFewestHopsRouteUseCase
import domain.usecase.FindOptimalPathUseCase
import domain.usecase.DispatchVehicleUseCase
import domain.usecase.OptimizeCargoByWeightUseCase
import domain.usecase.OptimizeCargoByWeightAndVolumeUseCase
import domain.usecase.ClassifyPackageSectorUseCase
import domain.usecase.TrackActiveFleetUseCase

import domain.model.Package
import domain.model.request.DispatchVehicleRequest
import domain.algorithm.knn.CargoFeatures
import domain.service.TelemetryStreamingService

import presentation.cli.InteractiveCli

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import org.koin.core.Koin
import org.koin.core.context.startKoin



fun main(): Unit = runBlocking {

    val koin = startKoin {
        modules(
            networkModule,
            repositoryModule,
            validatorModule,
            useCaseModule,
            telemetryModule,
            dispatcherModule,
            presentationModule
        )
    }.koin

    Main(koin).run()
}


class Main(
    private val koin: Koin
) {

    private val warehouseRepository =
        koin.get<WarehouseRepository>()

    private val packageRepository =
        koin.get<PackageRepository>()

    private val vehicleRepository =
        koin.get<VehicleRepository>()

    private val routeRepository =
        koin.get<RouteRepository>()

    private val findOptimalPathUseCase =
        koin.get<FindOptimalPathUseCase>()

    private val findFewestHopsRouteUseCase =
        koin.get<FindFewestHopsRouteUseCase>()

    private val analyzePackageDestinationDemandUseCase =
        koin.get<AnalyzePackageDestinationDemandUseCase>()

    private val dispatchVehicleUseCase =
        koin.get<DispatchVehicleUseCase>()

    private val weightOnlyOptimizer =
        koin.get<OptimizeCargoByWeightUseCase>()

    private val twoDimensionalOptimizer =
        koin.get<OptimizeCargoByWeightAndVolumeUseCase>()

    private val classifyPackageSectorUseCase =
        koin.get<ClassifyPackageSectorUseCase>()

    // Task 4: Telemetry dependencies
    private val telemetryStreamingService =
        koin.get<TelemetryStreamingService>()

    private val trackActiveFleetUseCase =
        koin.get<TrackActiveFleetUseCase>()


    suspend fun run() {
        refreshData()
        initializeRelationships()
        demoRepositories()
        demoAlgorithms()
        startInteractiveCli()
    }


    private suspend fun startInteractiveCli() {
        koin.get<InteractiveCli>().start()
    }

    private suspend fun refreshData() {
        println("\n=== Refreshing Data ===")

        coroutineScope {
            val warehousesDeferred = async {
                warehouseRepository.refresh().getOrThrow()
            }

            val packagesDeferred = async {
                packageRepository.refresh().getOrThrow()
            }
            val vehiclesDeferred = async {
                vehicleRepository.refresh().getOrThrow()
            }

            val routesDeferred = async {
                routeRepository.refresh().getOrThrow()
            }

            warehousesDeferred.await()
            packagesDeferred.await()
            vehiclesDeferred.await()
            routesDeferred.await()
        }

        println("All repositories refreshed successfully.")
    }


    private suspend fun initializeRelationships() {
        println("\n=== Initializing Relationships ===")

        packageRepository.initializeCargoQueues()
            .onSuccess {
                println("Cargo queues initialized.")
            }
            .onFailure {
                println("Cargo queues failed: ${it.message}")
            }

        vehicleRepository.initializeStationedVehicles()
            .onSuccess {
                println("Stationed vehicles initialized.")
            }
            .onFailure {
                println("Stationed vehicles failed: ${it.message}")
            }

        routeRepository.initializeOutgoingRoutes()
            .onSuccess {
                println("Outgoing routes initialized.")
            }
            .onFailure {
                println("Outgoing routes failed: ${it.message}")
            }
    }


    private suspend fun demoRepositories() {
        printWarehouses()
        printPackages()
        printVehicles()
        printRoutes()
    }


    private suspend fun printWarehouses() {
        println("\n=== Warehouses ===")

        warehouseRepository.findAll()
            .onSuccess { warehouses ->
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

        packageRepository.findAll()
            .onSuccess { packages ->
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
            .onSuccess { vehicles ->
                println("Count: ${vehicles.size}")

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

        routeRepository.findAll()
            .onSuccess { routes ->
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

        demoOptimalPath(
            testPackage.originWarehouse.id,
            testPackage.destinationWarehouse.id
        )

        demoFewestHops(
            testPackage.originWarehouse.id,
            testPackage.destinationWarehouse.id
        )

        demoDestinationDemand()

        demoCargoOptimizationComparison()

        dispatchVehicle()

        demoKnnSectorClassification()
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

        val warehouse =
            analyzePackageDestinationDemandUseCase(packages)

        if (warehouse != null) {
            println(
                "Most demanded destination: " +
                        "${warehouse.id}, ${warehouse.name}"
            )
        } else {
            println("No package demand found.")
        }
    }


    private suspend fun demoCargoOptimizationComparison() {
        println("\n=== Cargo Optimization Comparison ===")

        val demoPackages = createDemoPackages() ?: return

        val weightCapacityKg = 10.0
        val volumeCapacityM3 = 10.0

        printOptimizationCapacities(
            weightCapacityKg = weightCapacityKg,
            volumeCapacityM3 = volumeCapacityM3
        )

        compareOptimizers(
            packages = demoPackages,
            weightCapacityKg = weightCapacityKg,
            volumeCapacityM3 = volumeCapacityM3
        )
    }


    private suspend fun createDemoPackages(): List<Package>? {
        val warehouses = warehouseRepository.findAll().getOrThrow()

        if (warehouses.size < 2) {
            println(
                "Not enough warehouses available for cargo optimization demo."
            )
            return null
        }

        val originWarehouse = warehouses[0]
        val destinationWarehouse = warehouses[1]
        return listOf(
            Package(
                id = "PKG-900001",
                weightByKg = 4.0,
                priority = "URGENT",
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse,
                volumeM3 = 8.0
            ),
            Package(
                id = "PKG-900002",
                weightByKg = 6.0,
                priority = "URGENT",
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse,
                volumeM3 = 5.0
            ),
            Package(
                id = "PKG-900003",
                weightByKg = 6.0,
                priority = "STANDARD",
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse,
                volumeM3 = 2.0
            )
        )
    }


    private fun printOptimizationCapacities(
        weightCapacityKg: Double,
        volumeCapacityM3: Double
    ) {
        println("Weight capacity: $weightCapacityKg kg")
        println("Volume capacity: $volumeCapacityM3 m³")
    }


    private fun compareOptimizers(
        packages: List<Package>,
        weightCapacityKg: Double,
        volumeCapacityM3: Double
    ) {
        val weightOnlySelection = weightOnlyOptimizer(
            packages = packages,
            capacityKg = weightCapacityKg
        )

        val twoDimensionalSelection = twoDimensionalOptimizer(
            packages = packages,
            capacityKg = weightCapacityKg,
            capacityVolumeM3 = volumeCapacityM3
        )

        printOptimizationResult(
            title = "Weight-Only Optimization",
            packages = weightOnlySelection
        )

        printOptimizationResult(
            title = "Weight + Volume Optimization",
            packages = twoDimensionalSelection
        )
    }


    private fun printOptimizationResult(
        title: String,
        packages: List<Package>
    ) {
        println("\n--- $title ---")

        if (packages.isEmpty()) {
            println("No packages selected.")
            return
        }

        packages.forEach { logisticsPackage ->
            println(
                "Package ${logisticsPackage.id} | " +
                        "Weight: ${logisticsPackage.weightByKg} kg | " +
                        "Volume: ${logisticsPackage.volumeM3} m³ | " +
                        "Priority: ${logisticsPackage.priority}"
            )
        }

        val totalWeight = packages.sumOf {
            it.weightByKg
        }

        val totalVolume = packages.sumOf {
            it.volumeM3 ?: 0.0
        }

        println("Selected packages: ${packages.size}")
        println("Total weight: $totalWeight kg")
        println("Total volume: $totalVolume m³")
    }


    private suspend fun dispatchVehicle() {
        println("\n=== Knapsack Cargo Dispatch ===")

        val packages = packageRepository.findAll().getOrThrow()

        if (packages.isEmpty()) {
            println("No packages available for dispatch.")
            return
        }

        val warehouseId = packages.first().originWarehouse.id

        val vehicle = vehicleRepository.findAll()
            .getOrThrow()
            .firstOrNull()

        if (vehicle == null) {
            println("No vehicle available for dispatch.")
            return
        }

        println("Warehouse: $warehouseId")
        println("Vehicle: ${vehicle.id}")
        println("Vehicle capacity: ${vehicle.maxCapacityKg} kg")

        val result = dispatchVehicleUseCase(
            DispatchVehicleRequest(
                vehicleId = vehicle.id,
                warehouseId = warehouseId
            )
        )

        printDispatchResult(result)
    }


    private fun printDispatchResult(result: Result<List<Package>>
    ) {
        result
            .onSuccess { dispatchedPackages ->
                if (dispatchedPackages.isEmpty()) {
                    println("No packages were selected for dispatch.")
                    return@onSuccess
                }

                println(
                    "Knapsack selected ${dispatchedPackages.size} package(s):"
                )

                dispatchedPackages.forEach { logisticsPackage ->
                    println(
                        "Package ${logisticsPackage.id} | " +
                                "Weight: ${logisticsPackage.weightByKg} kg | " +
                                "Priority: ${logisticsPackage.priority} | " +
                                "State: ${logisticsPackage.state::class.simpleName}"
                    )
                }

                println(
                    "Successfully dispatched through BaseDispatchProcessor pipeline."
                )
            }
            .onFailure {
                println("Dispatch failed: ${it.message}")
            }
    }


    private suspend fun demoKnnSectorClassification() {
        println("\n=== KNN Sector Classification ===")

        val cargoToClassify = CargoFeatures(
            latitude = 31.55,
            longitude = 34.48,
            weightKg = 15.0
        )

        println("New Cargo:")
        println("Weight: ${cargoToClassify.weightKg} kg")
        println("Latitude: ${cargoToClassify.latitude}")
        println("Longitude: ${cargoToClassify.longitude}")

        classifyPackageSectorUseCase(cargoToClassify)
            .onSuccess { predictedSector ->
                println("Predicted Sector: $predictedSector")
            }
            .onFailure { exception ->
                println(
                    "Classification failed: ${exception.message}"
                )
            }
    }
}
