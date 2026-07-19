import parser.parseWarehousesFile
import java.io.File

private const val WAREHOUSE_FILE_PATH = "src/main/resources/warehouses.csv"

fun main() {

    println("--- Starting Logistics Data Initialization ---")

    val warehouseCsvFile = File(WAREHOUSE_FILE_PATH)
    val loadedWarehouses = parseWarehousesFile(warehouseCsvFile)

    println("Successfully parsed ${loadedWarehouses.size} valid warehouse records.")

    println("--- Detailed Warehouse Records ---")

    for (warehouse in loadedWarehouses) {
        println("ID: ${warehouse.id} | Name: ${warehouse.name} | Location: ${warehouse.regionalZone}")
    }
    println("--- Data Initialization Complete ---")
}