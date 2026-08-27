package validation

import domain.model.Route
import domain.model.Warehouse
import domain.routing.LeastHopRouter
import domain.routing.ShortestPathRouter
import domain.usecase.FindOptimalPathUseCase


class DijkstraRoutingValidation(
    private val warehouseIndex: Map<String, Warehouse>,
    private val router: ShortestPathRouter,
    private val findOptimalPathUseCase: FindOptimalPathUseCase

) {
    private val defaultRouteDelayMin = 10

    fun validateRouting() {
        validatePathExists()
        validateNoPath()
        validateSameWarehouse()
        validateCycle()
        validateShortestDistancePath()
        validateBfsComparison()
        validateIntegration()

        println("Dijkstra routing validation completed.")
    }

    private fun validatePathExists() {

        val startWarehouse = createWarehouse("WH-001")
        val middleWarehouse = createWarehouse("WH-002")
        val destinationWarehouse = createWarehouse("WH-003")
        startWarehouse.addRoute(
            createRoute(id = "RT-00001", origin = startWarehouse, destination = middleWarehouse, distanceKm = 5.0)
        )
        middleWarehouse.addRoute(
            createRoute(id = "RT-00002", origin = middleWarehouse, destination = destinationWarehouse, distanceKm = 4.0)
        )
        val router = ShortestPathRouter(
            listOf(
                startWarehouse,
                middleWarehouse,
                destinationWarehouse
            )
        )
        val warehousePath = router.findShortestPath(
            startWarehouse, destinationWarehouse
        )
        validateWarehousePath(
            testName = "Path exists",
            actualPath = warehousePath,
            expectedPath = listOf(
                "WH-001",
                "WH-002",
                "WH-003"
            )
        )
    }

    private fun validateNoPath() {

        val startWarehouse = createWarehouse("WH-001")
        val destinationWarehouse = createWarehouse("WH-002")
        val router = ShortestPathRouter(
            listOf(
                startWarehouse,
                destinationWarehouse
            )
        )
        val warehousePath =
            router.findShortestPath(
                startWarehouse,
                destinationWarehouse
            )
        if (warehousePath.isEmpty()) {
            println("No path: PASSED")
        } else {
            println("No path: FAILED")
            println("Expected: empty path")
            println("Actual: " + warehousePath.map { warehouse -> warehouse.id })
        }
    }

    private fun validateSameWarehouse() {

        val warehouse = createWarehouse("WH-001")
        val router = ShortestPathRouter(listOf(warehouse))
        val warehousePath = router.findShortestPath(
            warehouse,
            warehouse
        )
        validateWarehousePath(
            testName = "Same warehouse",
            actualPath = warehousePath,
            expectedPath = listOf("WH-001")
        )
    }

    private fun validateCycle() {

        val startWarehouse = createWarehouse("WH-001")
        val middleWarehouse = createWarehouse("WH-002")
        val destinationWarehouse = createWarehouse("WH-003")

        startWarehouse.addRoute(
            createRoute(id = "RT-00001", origin = startWarehouse, destination = middleWarehouse, distanceKm = 3.0)
        )
        middleWarehouse.addRoute(
            createRoute(id = "RT-00002", origin = middleWarehouse, destination = destinationWarehouse, distanceKm = 4.0)
        )
        destinationWarehouse.addRoute(
            createRoute(id = "RT-00003", origin = destinationWarehouse, destination = startWarehouse, distanceKm = 2.0)
        )
        val router = ShortestPathRouter(
            listOf(
                startWarehouse,
                middleWarehouse,
                destinationWarehouse
            )
        )
        val warehousePath = router.findShortestPath(
            startWarehouse, destinationWarehouse
        )
        validateWarehousePath(
            testName = "Cycle",
            actualPath = warehousePath,
            expectedPath = listOf(
                "WH-001",
                "WH-002",
                "WH-003"
            )
        )
    }

    private fun validateShortestDistancePath() {

        val warehouses = createRoutingComparisonGraph()
        val router = ShortestPathRouter(warehouses)
        val warehousePath = router.findShortestPath(
            warehouses[0],
            warehouses[3]
        )
        validateWarehousePath(
            testName = "Shortest-distance path",
            actualPath = warehousePath,
            expectedPath = listOf(
                "WH-001",
                "WH-003",
                "WH-002",
                "WH-004"
            )
        )
    }

    private fun createRoutingComparisonGraph(): List<Warehouse> {

        val startWarehouse = createWarehouse("WH-001")
        val shortHopWarehouse = createWarehouse("WH-002")
        val lowerDistanceWarehouse = createWarehouse("WH-003")
        val destinationWarehouse = createWarehouse("WH-004")

        startWarehouse.addRoute(
            createRoute("RT-00001", startWarehouse, shortHopWarehouse, 10.0)
        )
        shortHopWarehouse.addRoute(
            createRoute("RT-00002", shortHopWarehouse, destinationWarehouse, 4.0)
        )
        startWarehouse.addRoute(
            createRoute("RT-00003", startWarehouse, lowerDistanceWarehouse, 3.0)
        )
        lowerDistanceWarehouse.addRoute(
            createRoute("RT-00004", lowerDistanceWarehouse, shortHopWarehouse, 2.0)
        )
        return listOf(
            startWarehouse,
            shortHopWarehouse,
            lowerDistanceWarehouse,
            destinationWarehouse
        )
    }

    private fun validateBfsComparison() {

        val warehouses = createRoutingComparisonGraph()
        val startWarehouse = warehouses.first()
        val destinationWarehouse = warehouses.last()

        val bfsPath = LeastHopRouter().findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )
        val dijkstraPath =
            ShortestPathRouter(warehouses).findShortestPath(
                startWarehouse,
                destinationWarehouse
            )
        printRoutingComparison(
            bfsPath,
            dijkstraPath
        )
        validateRoutingComparison(
            bfsPath,
            dijkstraPath
        )
    }

    private fun printRoutingComparison(
        bfsPath: List<Warehouse>,
        dijkstraPath: List<Warehouse>
    ) {
        println()
        println("=== BFS vs Dijkstra Comparison ===")
        print("BFS Path: ")
        for (warehouse in bfsPath) {
            print("${warehouse.id} ")
        }
        println()
        print("Dijkstra Path: ")
        for (warehouse in dijkstraPath) {
            print("${warehouse.id} ")
        }
        println()
    }

    private fun validateRoutingComparison(
        bfsPath: List<Warehouse>,
        dijkstraPath: List<Warehouse>
    ) {
        val bfsWarehouseIds = bfsPath.map { warehouse ->
            warehouse.id
        }
        val dijkstraWarehouseIds = dijkstraPath.map { warehouse ->
            warehouse.id
        }
        if (
            bfsWarehouseIds != dijkstraWarehouseIds &&
            dijkstraPath.isNotEmpty()
        ) {
            println("BFS vs Dijkstra comparison: PASSED")
        } else {
            println("BFS vs Dijkstra comparison: FAILED")
        }
    }

    private fun validateIntegration() {
        val startWarehouse = warehouseIndex["WH-001"]
        val destinationWarehouse = warehouseIndex["WH-002"]

        if (startWarehouse == null || destinationWarehouse == null) {
            println("Dijkstra integration: FAILED")
            println("Required warehouses were not found.")
            return
        }
        val warehousePath = findOptimalPathUseCase(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        if (warehousePath.isNotEmpty()) {
            println("Dijkstra integration: PASSED")
            print("Warehouse Path: ")
            for (warehouse in warehousePath) print("${warehouse.id} ")
            println()
        } else {
            println("Dijkstra integration: FAILED")
            println("No path found between " + "${startWarehouse.id} and " + destinationWarehouse.id)
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
        destination: Warehouse,
        distanceKm: Double
    ): Route {
        return Route(
            id = id,
            originWarehouse = origin,
            destinationWarehouse = destination,
            distanceKm = distanceKm,
            typicalDelayMin = defaultRouteDelayMin
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
