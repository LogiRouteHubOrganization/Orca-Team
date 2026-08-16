
package validation

import domain.model.Route
import domain.model.Warehouse
import domain.routing.LeastHopRouter

class LeastHopRoutingValidation
    (private val warehouseIndex: Map<String, Warehouse>)
{

    private val router = LeastHopRouter()

    fun validateRouting() {
        validatePathExists()
        validateNoPath()
        validateSameWarehouse()
        validateCycle()
        validateLeastHopPath()
        validateIntegration()

        println("Routing validation completed.")
    }

    private fun validatePathExists() {
        val startWarehouse = createWarehouse("WH-001")
        val destinationWarehouse = createWarehouse("WH-002")

        startWarehouse.addRoute(
            createRoute(
                "RT-00001",
                startWarehouse,
                destinationWarehouse
            )
        )

        val warehousePath = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        validateWarehousePath(
            "Direct path",
            warehousePath,
            listOf("WH-001", "WH-002")
        )
    }

    private fun validateNoPath() {
        val startWarehouse = createWarehouse("WH-001")
        val destinationWarehouse = createWarehouse("WH-002")

        val warehousePath = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (warehousePath.isEmpty()) {
            println("No path: PASSED")
        } else {
            println("No path: FAILED")
            println("Expected: empty path")
            println("Actual:   ${warehousePath.map { it.id }}")
        }
    }

    private fun validateSameWarehouse() {
        val warehouse = createWarehouse("WH-001")

        val warehousePath = router.findLeastHopPath(
            warehouse,
            warehouse
        )

        validateWarehousePath(
            "Same warehouse",
            warehousePath,
            listOf("WH-001")
        )
    }

    private fun validateCycle() {
        val startWarehouse = createWarehouse("WH-001")
        val middleWarehouse = createWarehouse("WH-002")
        val destinationWarehouse = createWarehouse("WH-003")

        startWarehouse.addRoute(
            createRoute(
                "RT-00001",
                startWarehouse,
                middleWarehouse
            )
        )

        middleWarehouse.addRoute(
            createRoute(
                "RT-00002",
                middleWarehouse,
                destinationWarehouse
            )
        )

        destinationWarehouse.addRoute(
            createRoute(
                "RT-00003",
                destinationWarehouse,
                startWarehouse
            )
        )

        val warehousePath = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        validateWarehousePath(
            "Cycle",
            warehousePath,
            listOf("WH-001", "WH-002", "WH-003")
        )
    }

    private fun validateLeastHopPath() {
        val startWarehouse = createWarehouse("WH-001")
        val shortPathWarehouse = createWarehouse("WH-002")
        val alternativePathWarehouse = createWarehouse("WH-003")
        val destinationWarehouse = createWarehouse("WH-004")
        val extraPathWarehouse = createWarehouse("WH-005")

        startWarehouse.addRoute(
            createRoute(
                "RT-00001",
                startWarehouse,
                shortPathWarehouse
            )
        )

        shortPathWarehouse.addRoute(
            createRoute(
                "RT-00002",
                shortPathWarehouse,
                destinationWarehouse
            )
        )

        startWarehouse.addRoute(
            createRoute(
                "RT-00003",
                startWarehouse,
                alternativePathWarehouse
            )
        )

        alternativePathWarehouse.addRoute(
            createRoute(
                "RT-00004",
                alternativePathWarehouse,
                extraPathWarehouse
            )
        )

        extraPathWarehouse.addRoute(
            createRoute(
                "RT-00005",
                extraPathWarehouse,
                destinationWarehouse
            )
        )

        val warehousePath = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        validateWarehousePath(
            "Least-hop path",
            warehousePath,
            listOf("WH-001", "WH-002", "WH-004")
        )
    }
    private fun validateIntegration() {
        val startWarehouse = warehouseIndex["WH-001"]
        val destinationWarehouse = warehouseIndex["WH-002"]

        if (startWarehouse == null || destinationWarehouse == null) {
            println("Integration: FAILED")
            println("Required warehouses were not found.")
            return
        }

        val warehousePath = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (warehousePath.isNotEmpty()) {
            println("Integration: PASSED")
            println(
                "WarehousePath: ${warehousePath.map { warehouse -> warehouse.id }}"
            )
        } else {
            println("Integration: FAILED")
            println("No path found between WH-001 and WH-002.")
        }
    }

    private fun createWarehouse(
        id: String
    ): Warehouse {

        return Warehouse(
            id = id,
            name = "Test Warehouse",
            regionalZone = "TEST",
            latitude = 0.0,
            longitude = 0.0
        )
    }

    private fun createRoute(
        id: String,
        origin: Warehouse,
        destination: Warehouse
    ): Route {

        return Route(
            id = id,
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = 100.0,
            typicalDelayMin = 10
        )
    }
    private fun validateWarehousePath(
        testName: String,
        actualPath: List<Warehouse>,
        expectedPath: List<String>
    ) {
        val actualWarehouseIds = actualPath.map { warehouse -> warehouse.id }

        if (actualWarehouseIds == expectedPath) {
            println("$testName: PASSED")
        } else {
            println("$testName: FAILED")
            println("Expected: $expectedPath")
            println("Actual:   $actualWarehouseIds")
        }
    }
}
