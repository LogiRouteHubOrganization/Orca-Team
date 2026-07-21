import parser.PackageParser
import parser.WarehouseParser
import parser.parseRoute
fun main() {
    val packageFilePath = "src/main/resources/packages.csv"
    val packageParser = PackageParser()

    val packages = packageParser.parse(packageFilePath)

    packages.forEach { logisticsPackage ->
        println(logisticsPackage)

        println("\n******************************************************\n")

        val warehouseFilePath = "src/main/resources/warehouses.csv"

        println("Starting Warehouse Data Pipeline...")
        val parsedWarehouses = WarehouseParser.parseWarehouses(packageFilePath)

        println("\n--- Verification Results ---")
        println("Successfully parsed records count: ${parsedWarehouses.size}")

        println("\nSample of Parsed Warehouses:")
        parsedWarehouses.take(5).forEach { warehouse ->
            println(warehouse)

        }




    }

print("..............................................")

    val routeFilePath = "src/main/resources/routes.csv"

    val routes = parseRoute(routeFilePath)

    for (route in routes) {
        println(route)
    }
    }