import parser.parseWarehousesFile
import java.io.File

private const val WAREHOUSE_FILE_PATH = "src/main/resources/warehouses.csv"

import parser.PackageParser
import parser.WarehouseParser

fun main() {
  val packageFilePath = "src/main/resources/packages.csv"
  val packageParser = PackageParser()

  val packages = packageParser.parse( packageFilePath)

    println("--- Starting Logistics Data Initialization ---")

    val warehouseCsvFile = File(WAREHOUSE_FILE_PATH)
    val loadedWarehouses = parseWarehousesFile(warehouseCsvFile)

    println("Successfully parsed ${loadedWarehouses.size} valid warehouse records.")
  packages.forEach { logisticsPackage ->
    println(logisticsPackage)
  }
 
  println("\n******************************************************\n")
  
    val warehouseFilePath = "src/main/resources/warehouses.csv"

    println("Starting Warehouse Data Pipeline...")
    val parsedWarehouses = WarehouseParser.parseWarehouses(warehouseFilePath)

    println("--- Detailed Warehouse Records ---")

    for (warehouse in loadedWarehouses) {
        println("ID: ${warehouse.warehouseId} | Name: ${warehouse.warehousename} | Location: ${warehouse.regionalZone}")
    }
    println("--- Data Initialization Complete ---")
}
    println("\nSample of Parsed Warehouses:")
    parsedWarehouses.take(5).forEach { warehouse ->
        println(warehouse) 
}
}
