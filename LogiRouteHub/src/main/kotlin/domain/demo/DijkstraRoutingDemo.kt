package domain.demo

import domain.model.Route
import domain.model.Warehouse
import domain.routing.LeastHopRouter
import domain.routing.ShortestPathRouter
import domain.usecase.FindOptimalPathUseCase

class DijkstraRoutingDemo(
    private val warehouseIndex: Map<String, Warehouse>,
    private val findOptimalPathUseCase: FindOptimalPathUseCase
) {
    private val defaultRouteDelayMin = 10

    suspend fun runDemo() {
        validatePathExists()
        validateNoPath()
        validateSameWarehouse()
        validateCycle()
        validateShortestDistancePath()
        validateBfsComparison()
        demonstrateIntegration()

        println("Dijkstra routing demo completed.")
    }

    private fun validatePathExists() {
        val start = createWarehouse("WH-000001")
        val middle = createWarehouse("WH-000002")
        val destination = createWarehouse("WH-000003")

        start.addRoute(createRoute("RT-000001", start, middle, 5.0))
        middle.addRoute(createRoute("RT-000002", middle, destination, 4.0))

        val router = ShortestPathRouter(listOf(start, middle, destination))
        val warehousePath = router.findShortestPath(start, destination)

        validateWarehousePath(
            testName = "Path exists",
            actualPath = warehousePath,
            expectedPath = listOf("WH-000001", "WH-000002", "WH-000003")
        )
    }

    private fun validateNoPath() {
        val start = createWarehouse("WH-000001")
        val destination = createWarehouse("WH-000002")

        val router = ShortestPathRouter(listOf(start, destination))
        val warehousePath = router.findShortestPath(start, destination)

        if (warehousePath.isEmpty()) {
            println("No path: PASSED")
        } else {
            println("No path: FAILED\nExpected: empty path")
            println("Actual: ${warehousePath.map { it.id }}")
        }
    }

    private fun validateSameWarehouse() {
        val warehouse = createWarehouse("WH-000001")
        val router = ShortestPathRouter(listOf(warehouse))
        val warehousePath = router.findShortestPath(warehouse, warehouse)

        validateWarehousePath(
            testName = "Same warehouse",
            actualPath = warehousePath,
            expectedPath = listOf("WH-000001")
        )
    }

    private fun validateCycle() {
        val start = createWarehouse("WH-000001")
        val middle = createWarehouse("WH-000002")
        val destination = createWarehouse("WH-000003")

        start.addRoute(createRoute("RT-000001", start, middle, 3.0))
        middle.addRoute(createRoute("RT-000002", middle, destination, 4.0))
        destination.addRoute(createRoute("RT-000003", destination, start, 2.0))

        val router = ShortestPathRouter(listOf(start, middle, destination))
        val warehousePath = router.findShortestPath(start, destination)

        validateWarehousePath(
            testName = "Cycle",
            actualPath = warehousePath,
            expectedPath = listOf("WH-000001", "WH-000002", "WH-000003")
        )
    }

    private fun validateShortestDistancePath() {
        val warehouses = createRoutingComparisonGraph()
        val router = ShortestPathRouter(warehouses)
        val warehousePath = router.findShortestPath(warehouses[0], warehouses[3])

        validateWarehousePath(
            testName = "Shortest-distance path",
            actualPath = warehousePath,
            expectedPath = listOf("WH-000001", "WH-000003", "WH-000002", "WH-000004")
        )
    }

    private fun createRoutingComparisonGraph(): List<Warehouse> {
        val start = createWarehouse("WH-000001")
        val shortHop = createWarehouse("WH-000002")
        val lowerDist = createWarehouse("WH-000003")
        val dest = createWarehouse("WH-000004")

        start.addRoute(createRoute("RT-000001", start, shortHop, 10.0))
        shortHop.addRoute(createRoute("RT-000002", shortHop, dest, 4.0))
        start.addRoute(createRoute("RT-000003", start, lowerDist, 3.0))
        lowerDist.addRoute(createRoute("RT-000004", lowerDist, shortHop, 2.0))

        return listOf(start, shortHop, lowerDist, dest)
    }

    private fun validateBfsComparison() {
        val warehouses = createRoutingComparisonGraph()
        val start = warehouses.first()
        val destination = warehouses.last()

        val bfsPath = LeastHopRouter().findLeastHopPath(start, destination)
        val dijkstraPath = ShortestPathRouter(warehouses).findShortestPath(start, destination)

        printRoutingComparison(bfsPath, dijkstraPath)
        validateRoutingComparison(bfsPath, dijkstraPath)
    }

    private fun printRoutingComparison(
        bfsPath: List<Warehouse>,
        dijkstraPath: List<Warehouse>
    ) {
        println("\n=== BFS vs Dijkstra Comparison ===")
        println("BFS Path: ${bfsPath.joinToString(" ") { it.id }}")
        println("Dijkstra Path: ${dijkstraPath.joinToString(" ") { it.id }}")
    }

    private fun validateRoutingComparison(
        bfsPath: List<Warehouse>,
        dijkstraPath: List<Warehouse>
    ) {
        val bfsIds = bfsPath.map { it.id }
        val dijkstraIds = dijkstraPath.map { it.id }

        if (bfsIds != dijkstraIds && dijkstraPath.isNotEmpty()) {
            println("BFS vs Dijkstra comparison: PASSED")
        } else {
            println("BFS vs Dijkstra comparison: FAILED")
        }
    }

    private suspend fun demonstrateIntegration() {
        val start = warehouseIndex["WH-000001"]
        val destination = warehouseIndex["WH-000002"]

        if (start == null || destination == null) {
            println("Dijkstra integration: FAILED\nRequired warehouses were not found.")
            return
        }

        findOptimalPathUseCase(start.id, destination.id)
            .onSuccess { path ->
                if (path.isNotEmpty()) {
                    println("Dijkstra integration: PASSED")
                    println("Warehouse Path: ${path.joinToString(" ") { it.id }}")
                } else {
                    println("Dijkstra integration: FAILED\nNo path found between ${start.id} and ${destination.id}")
                }
            }
            .onFailure { exception ->
                println("Dijkstra integration: FAILED\nError: ${exception.message ?: "Unknown error"}")
            }
    }

    private fun createWarehouse(id: String): Warehouse {
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
        val actualWarehouseIds = actualPath.map { it.id }

        if (actualWarehouseIds == expectedPath) {
            println("$testName: PASSED")
        } else {
            println("$testName: FAILED\nExpected: $expectedPath\nActual:   $actualWarehouseIds")
        }
    }
}

