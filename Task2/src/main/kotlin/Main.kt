
import java.io.File
import parser.parseWarehousesFile
import parser.parseRoute
import parser.PackageParser
fun main() {

  println("=== Package Parser ===")

  val packageFilePath = "src/main/resources/packages.csv"
  val packageParser = PackageParser()

  val logisticsPackageRows =
    packageParser.readLogisticsPackageRowsFromFile(packageFilePath)

  println()
  println("Successfully parsed packages: ${logisticsPackageRows.size}")
  println()

  logisticsPackageRows.forEach { logisticsPackageRow ->
    println(logisticsPackageRow)
  }

  println()
  println("Package parsing completed.")

    
  println("\n******************************************************\n")
  
    val WAREHOUSE_FILE_PATH = "src/main/resources/warehouses.csv"
    println("Starting Warehouse Data Pipeline...")
    val warehouseCsvFile = File(WAREHOUSE_FILE_PATH)
    val loadedWarehouses = parseWarehousesFile(warehouseCsvFile)
    println("Successfully parsed ${loadedWarehouses.size} valid warehouse records.")

    println("--- Detailed Warehouse Records ---")

for (warehouse in loadedWarehouses) {
        println("ID: ${warehouse.id} | Name: ${warehouse.name} | Location: ${warehouse.regionalZone}")
    }
    println("--- Data Initialization Complete ---")
    
    print("\n*************************************************************\n")

    val routeFilePath = "src/main/resources/routes.csv"

    val routes = parseRoute(routeFilePath)

    for (route in routes) {
        println(route)
    }
    

}

