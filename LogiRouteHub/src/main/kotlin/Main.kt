import algorithm.sortPackages
import parser.FleetParser
import parser.PackageParser
import parser.parseRoutes
import parser.parseWarehousesFile
import validation.validateDomainGraph
import builder.DomainGraphBuilder
import validation.validatePricingStrategies
import algorithm.PackageQuickSorter
import java.io.File

fun main() {

  println("=== Package Parser ===")

  val packageFilePath =
    "src/main/resources/packages.csv"

  val packageParser = PackageParser()

  val rawPackages =
    packageParser
      .readLogisticsPackageRowsFromFile(packageFilePath)
      .toMutableList()

  sortPackages(rawPackages)

  println()
  println("Successfully parsed packages: ${rawPackages.size}")
  println()
  println("Sorted packages:")

  for (rawPackage in rawPackages) {
    println(rawPackage)
  }

  println()
  println("Package sorting completed.")

  println("\n******************************************************\n")

  println("Starting Warehouse Data Pipeline...")

  val warehouseFilePath =
    "src/main/resources/warehouses.csv"

  val warehouseCsvFile =
    File(warehouseFilePath)

  val rawWarehouses =
    parseWarehousesFile(warehouseCsvFile)

  println(
    "Successfully parsed ${rawWarehouses.size} " +
            "valid warehouse records."
  )

  println("--- Detailed Warehouse Records ---")

  for (rawWarehouse in rawWarehouses) {
    println(
      "ID: ${rawWarehouse.warehouseId}" +
              " | Name: ${rawWarehouse.warehouseName}" +
              " | Zone: ${rawWarehouse.regionalZone}" +
              " | Latitude: ${rawWarehouse.latitude}" +
              " | Longitude: ${rawWarehouse.longitude}"
    )
  }

  println("--- Warehouse Data Initialization Complete ---")

  println("\n******************************************************\n")

  println("Starting Route Data Pipeline...")

  val routeFilePath =
    "src/main/resources/routes.csv"

  val rawRoutes =
    parseRoutes(routeFilePath)

  println(
    "Successfully parsed ${rawRoutes.size} valid route records."
  )

  println("--- Detailed Route Records ---")

  for (rawRoute in rawRoutes) {
    println(
      "Route ID: ${rawRoute.routeId}" +
              " | Origin Hub: ${rawRoute.originHubId}" +
              " | Destination Hub: ${rawRoute.destinationHubId}" +
              " | Distance: ${rawRoute.distanceKm} km" +
              " | Typical Delay: ${rawRoute.typicalDelayMin} min"
    )
  }

  println("--- Route Data Initialization Complete ---")

  println("\n******************************************************\n")

  println("Starting Fleet Data Pipeline...")

  val fleetFilePath =
    "src/main/resources/fleet.csv"

  val fleetParser = FleetParser()

  val rawVehicles =
    fleetParser.readFleetRowsFromFile(fleetFilePath)

  println(
    "Successfully parsed ${rawVehicles.size} valid fleet records."
  )

  println("--- Detailed Fleet Records ---")

  for (rawVehicle in rawVehicles) {
    println(
      "Vehicle ID: ${rawVehicle.vehicleId}" +
              " | Current Hub: ${rawVehicle.currentHubId}" +
              " | Capacity: ${rawVehicle.maxCapacityKg} kg" +
              " | Cost/Km: ${rawVehicle.costPerKm}"
    )
  }

  println("--- Fleet Data Initialization Complete ---")

  // Verify that packages, vehicles, and routes reference the same warehouse objects.

  val domainGraphBuilder = DomainGraphBuilder()
  val warehousesById =
    domainGraphBuilder.buildGraph(
      rawWarehouses = rawWarehouses,
      rawPackages = rawPackages,
      rawVehicles = rawVehicles,
      rawRoutes = rawRoutes
    )

  validateDomainGraph(warehousesById)

  val packageQuickSorter = PackageQuickSorter()

  packageQuickSorter.sortAllCargoQueuesByDescendingWeight(
    warehousesById
  )
  println()
  println("=== QuickSort Validation ===")

  val firstWarehouse =
    warehousesById.values.first()

  println(
    "Warehouse: ${firstWarehouse.warehouseId}"
  )

  println()

  println("Cargo Queue After Sorting:")

  for (logisticsPackage in firstWarehouse.cargoQueue) {

    println(
      "${logisticsPackage.packageId} | " +
              "${logisticsPackage.packageWeightByKg} kg"
    )
  }
  // Validate pricing calculations using the generated domain graph.

  validatePricingStrategies(warehousesById)
}