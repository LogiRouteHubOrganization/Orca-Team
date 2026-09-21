package domain.demo

import domain.model.Route
import domain.model.Warehouse
import domain.routing.LeastHopRouter
import domain.usecase.FindFewestHopsRouteUseCase

class LeastHopRoutingDemo(
    private val warehouseIndex: Map<String, Warehouse>,
    private val findFewestHopsRouteUseCase: FindFewestHopsRouteUseCase
) {

    suspend fun runDemo() {
        validatePathExists()
        validateNoPath()
        validateSameWarehouse()
        validateCycle()
        validateLeastHopPath()
        demonstrateIntegration()

        println("Least-hop routing demo completed.")
    }

    private fun validatePathExists() {
        val startWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        startWarehouse.addRoute(
            createRoute(
                id = "RT-000001",
                origin = startWarehouse,
                destination = destinationWarehouse
            )
        )

        val warehousePath = LeastHopRouter().findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        validateWarehousePath(
            testName = "Direct path",
            actualPath = warehousePath,
            expectedPath = listOf(
                "WH-000001",
                "WH-000002"
            )
        )
    }

    private fun validateNoPath() {
        val startWarehouse = createWarehouse("WH-000001")
        val destinationWarehouse = createWarehouse("WH-000002")

        val warehousePath = LeastHopRouter().findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (warehousePath.isEmpty()) {
            println("No path: PASSED")
        } else {
            println("No path: FAILED")
            println("Expected: empty path")
            println(
                "Actual: ${warehousePath.map { warehouse -> warehouse.id }}"
            )
        }
    }

    private fun validateSameWarehouse() {
        val warehouse = createWarehouse("WH-000001")

        val warehousePath = LeastHopRouter().findLeastHopPath(
            warehouse,
            warehouse
        )

        validateWarehousePath(
            testName = "Same warehouse",
            actualPath = warehousePath,
            expectedPath = listOf("WH-000001")
        )
    }

    private fun validateCycle() {
        val startWarehouse = createWarehouse("WH-000001")
        val middleWarehouse = createWarehouse("WH-000002")
        val destinationWarehouse = createWarehouse("WH-000003")

        startWarehouse.addRoute(
            createRoute(
                id = "RT-000001",
                origin = startWarehouse,
                destination = middleWarehouse
            )
        )

        middleWarehouse.addRoute(
            createRoute(
                id = "RT-000002",
                origin = middleWarehouse,
                destination = destinationWarehouse
            )
        )

        destinationWarehouse.addRoute(
            createRoute(
                id = "RT-000003",
                origin = destinationWarehouse,
                destination = startWarehouse
            )
        )

        val warehousePath = LeastHopRouter().findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        validateWarehousePath(
            testName = "Cycle",
            actualPath = warehousePath,
            expectedPath = listOf(
                "WH-000001",
                "WH-000002",
                "WH-000003"
            )
        )
    }

    private fun validateLeastHopPath() {
        val startWarehouse = createWarehouse("WH-000001")
        val shortPathWarehouse = createWarehouse("WH-000002")
        val alternativePathWarehouse = createWarehouse("WH-000003")
        val destinationWarehouse = createWarehouse("WH-000004")
        val extraPathWarehouse = createWarehouse("WH-000005")

        startWarehouse.addRoute(
            createRoute(
                id = "RT-000001",
                origin = startWarehouse,
                destination = shortPathWarehouse
            )
        )

        shortPathWarehouse.addRoute(
            createRoute(
                id = "RT-000002",
                origin = shortPathWarehouse,
                destination = destinationWarehouse
            )
        )

        startWarehouse.addRoute(
            createRoute(
                id = "RT-000003",
                origin = startWarehouse,
                destination = alternativePathWarehouse
            )
        )

        alternativePathWarehouse.addRoute(
            createRoute(
                id = "RT-000004",
                origin = alternativePathWarehouse,
                destination = extraPathWarehouse
            )
        )

        extraPathWarehouse.addRoute(
            createRoute(
                id = "RT-000005",
                origin = extraPathWarehouse,
                destination = destinationWarehouse
            )
        )

        val warehousePath = LeastHopRouter().findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        validateWarehousePath(
            testName = "Least-hop path",
            actualPath = warehousePath,
            expectedPath = listOf(
                "WH-000001",
                "WH-000002",
                "WH-000004"
            )
        )
    }

    private suspend fun demonstrateIntegration() {
        val startWarehouse = warehouseIndex["WH-000001"]
        val destinationWarehouse = warehouseIndex["WH-000002"]

        if (startWarehouse == null || destinationWarehouse == null) {
            println("Least-hop integration: FAILED")
            println("Required warehouses were not found.")
            return
        }

        findFewestHopsRouteUseCase(
            startWarehouseId = startWarehouse.id,
            destinationWarehouseId = destinationWarehouse.id
        ).onSuccess { warehousePath ->

            if (warehousePath.isNotEmpty()) {
                println("Least-hop integration: PASSED")
                println(
                    "Warehouse Path: ${
                        warehousePath.map { warehouse -> warehouse.id }
                    }"
                )
            } else {
                println("Least-hop integration: FAILED")
                println(
                    "No path found between " +
                            "${startWarehouse.id} and ${destinationWarehouse.id}."
                )
            }

        }.onFailure { exception ->

            println("Least-hop integration: FAILED")
            println(
                "Error: ${exception.message ?: "Unknown error"}"
            )
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
        val actualWarehouseIds = actualPath.map { warehouse ->
            warehouse.id
        }

        if (actualWarehouseIds == expectedPath) {
            println("$testName: PASSED")
        } else {
            println("$testName: FAILED")
            println("Expected: $expectedPath")
            println("Actual:   $actualWarehouseIds")
        }
    }
}
