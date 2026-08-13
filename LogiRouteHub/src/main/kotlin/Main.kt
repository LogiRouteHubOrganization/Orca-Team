import algorithm.PackageQuickSorter
import algorithm.sortPackages
import dataHolder.FleetRaw
import dataHolder.LogisticsPackageRaw
import dataHolder.RouteRaw
import dataHolder.WarehouseRaw
import domain.builder.DomainGraphBuilder
import domain.builder.GraphBuilderInput
import domain.model.Warehouse
import parser.FleetParser
import parser.PackageParser
import parser.RouteParser
import parser.WarehouseParser
import repository.PackageRepository
import repository.WarehouseRepository
import repository.csv.CsvPackageRepository
import repository.csv.CsvWarehouseRepository
import validation.validateDomainGraph
import validation.validatePricingStrategies
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

    val packageParser = PackageParser()
    val packageRepository: PackageRepository = CsvPackageRepository(
        packageFile = File(PACKAGES_FILE_PATH),
        packageParser = packageParser
    )

    val warehouseParser =
        WarehouseParser()

    val warehouseRepository: WarehouseRepository = CsvWarehouseRepository(
        warehouseFile = File(WAREHOUSES_FILE_PATH),
        warehouseParser = warehouseParser
    )

    val rawPackages =
        loadPackages(
            packageRepository
        )

    sortAndDisplayPackages(
        rawPackages
    )
    val rawWarehouses = loadWarehouses(warehouseRepository)

    val rawRoutes = loadRoutes()

    val rawVehicles = loadVehicles()

    val warehouseIndex = buildDomainGraph(
        rawPackages = rawPackages,
        rawWarehouses = rawWarehouses,
        rawRoutes = rawRoutes,
        rawVehicles = rawVehicles
    )

    validateDomainGraph(warehouseIndex)

    validateQuickSort(warehouseIndex)

    validatePricingStrategies(warehouseIndex)
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
    println(
        "Top $TOP_PACKAGE_COUNT priority packages:"
    )

    val displayedPackageCount =
        minOf(
            TOP_PACKAGE_COUNT,
            packages.size
        )

    for (
    packageIndex in
    packages.indices.take(
        displayedPackageCount
    )
    ) {
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

    println("Successfully loaded ${warehouses.size} " + "valid warehouse records.")

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

private fun loadRoutes(): List<RouteRaw> {

    println("Starting Route Data Pipeline...")

    val routes = RouteParser(ROUTES_FILE_PATH)

    println("Successfully parsed ${routes.size} " + "valid route records.")

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

private fun loadVehicles(): List<FleetRaw> {

    println("Starting Fleet Data Pipeline...")

    val vehicles = FleetParser().readFleetRowsFromFile(FLEET_FILE_PATH)

    println("Successfully parsed ${vehicles.size} " + "valid fleet records.")

    println(
        "--- Detailed Fleet Records ---"
    )

    for (vehicle in vehicles) {
        println(
            "Vehicle ID: ${vehicle.vehicleId}" +
                    " | Current Hub: ${vehicle.currentHubId}" +
                    " | Capacity: ${vehicle.maxCapacityKg} kg" +
                    " | Cost/Km: ${vehicle.costPerKm}"
        )
    }

    println(
        "--- Fleet Data Initialization Complete ---"
    )

    printSeparator()

    return vehicles
}

private fun buildDomainGraph(
    rawPackages: List<LogisticsPackageRaw>,
    rawWarehouses: List<WarehouseRaw>,
    rawRoutes: List<RouteRaw>,
    rawVehicles: List<FleetRaw>
): Map<String, Warehouse> {

    val graphBuilderInput = GraphBuilderInput(
        warehouses = rawWarehouses,
        packages = rawPackages,
        vehicles = rawVehicles,
        routes = rawRoutes
    )

    val domainGraphBuilder = DomainGraphBuilder()

    return domainGraphBuilder.buildGraph(graphBuilderInput)
}

private fun validateQuickSort(
    warehouseIndex: Map<String, Warehouse>
) {
    println()

    println(
        "=== QuickSort Validation ==="
    )
    val firstWarehouse = warehouseIndex.values.firstOrNull()

    if (firstWarehouse == null) {
        println("QuickSort validation skipped: " + "No warehouses were built.")

        printSeparator()

        return
    }

    println("Warehouse: ${firstWarehouse.id}")
    println()

    println("Cargo Queue Before Sorting:")

    printCargoQueue(firstWarehouse)

    val packageQuickSorter = PackageQuickSorter()

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
            "${logisticsPackage.id}" +
                    " | ${formatWeight(logisticsPackage.weightByKg)} kg"
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