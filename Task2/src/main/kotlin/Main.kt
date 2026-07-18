import parser.WarehouseParser

fun main() {

    val filePath = "src/main/resources/warehouses.csv"

    println("Starting Warehouse Data Pipeline...")
    val parsedWarehouses = WarehouseParser.parseWarehouses(filePath)

    println("\n--- Verification Results ---")
    println("Successfully parsed records count: ${parsedWarehouses.size}")

    println("\nSample of Parsed Warehouses:")
    parsedWarehouses.take(5).forEach { warehouse ->
        println(warehouse)
    }
}