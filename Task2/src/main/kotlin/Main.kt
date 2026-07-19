
import parser.PackageParser
import parser.WarehouseParser

fun main() {
  val packageFilePath = "src/main/resources/packages.csv"
  val packageParser = PackageParser()

  val packages = packageParser.parse( packageFilePath)

  packages.forEach { logisticsPackage ->
    println(logisticsPackage)
 
  println("\n******************************************************\n")
  
    val warehouseFilePath = "src/main/resources/warehouses.csv"

    println("Starting Warehouse Data Pipeline...")
    val parsedWarehouses = WarehouseParser.parseWarehouses(warehouseFilePath)

    println("\n--- Verification Results ---")
    println("Successfully parsed records count: ${parsedWarehouses.size}")

    println("\nSample of Parsed Warehouses:")
    parsedWarehouses.take(5).forEach { warehouse ->
        println(warehouse)
   
}
