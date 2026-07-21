import parser.parseWarehousesFile
import java.io.File
import parser.PackageParser
fun main() {

  val packageFilePath = "src/main/resources/packages.csv"
  val packageParser = PackageParser()

  val logisticsPackageRows =
    packageParser.readLogisticsPackageRowsFromFile(packageFilePath)

  logisticsPackageRows.forEach { logisticsPackageRow ->
    println(logisticsPackageRow)
    
}
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

}
