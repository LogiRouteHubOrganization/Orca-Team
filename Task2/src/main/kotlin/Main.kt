import java.io.File
import parser.parseWarehousesFile
import parser.parseRoutes
import algorithm.sortPackages
import parser.PackageParser
import parser.FleetParser

fun main() {

  println("=== Package Parser ===")

  val packageFilePath = "src/main/resources/packages.csv"

  val packageParser = PackageParser()

  val logisticsPackageRows =
    packageParser
      .readLogisticsPackageRowsFromFile(packageFilePath)
      .toMutableList()

  sortPackages(logisticsPackageRows)

  println()
  println("Successfully parsed packages: ${logisticsPackageRows.size}")
  println()

  println("Sorted packages:")

  logisticsPackageRows.forEach { logisticsPackageRow ->
    println(logisticsPackageRow)
  }

  println()
  println("Package sorting completed.")
  
  println("\n*****************************************************************\n")
  
    val WAREHOUSE_FILE_PATH = "src/main/resources/warehouses.csv"
    println("Starting Warehouse Data Pipeline...")
    val warehouseCsvFile = File(WAREHOUSE_FILE_PATH)
    val loadedWarehouses = parseWarehousesFile(warehouseCsvFile)
    println("Successfully parsed ${loadedWarehouses.size} valid warehouse records.")

    println("--- Detailed Warehouse Records ---")

    for (warehouse in loadedWarehouses) {
        println("ID: ${warehouse.warehouseId} | Name: ${warehouse.warehousename} | Location: ${warehouse.regionalZone}")
    }
    println("--- Data Initialization Complete ---")

    println("\n*****************************************************************\n")

    val routeFilePath = "src/main/resources/routes.csv"

    val routes =  parseRoutes(routeFilePath)

    for (route in routes) {
        println(route)
    }
    println("\n*****************************************************************\n")

    println("===== Fleet Parser =====")

  val fleetFilePath = "src/main/resources/fleet.csv"

  val fleetParser = FleetParser()

  val fleets = fleetParser.readFleetRowsFromFile(fleetFilePath)

  println("Successfully parsed ${fleets.size} valid fleet records.")

  println("--- Detailed Fleet Records ---")

  for (fleet in fleets) {
      println("Vehicle ID: ( ${fleet.vehicleId} , Hub: ${fleet.currentHubId} , Capacity: ${fleet.maxCapacityKg} , Cost/Km: ${fleet.costPerKm})") }
     println("--- Fleet Data Initialization Complete ---")

}

