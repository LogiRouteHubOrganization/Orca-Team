package validation

import domain.model.Route
import domain.model.Warehouse
import domain.routing.LeastHopRouter

class LeastHopRoutingValidation {

    private val router = LeastHopRouter()

    fun runRoutingValidation(
        warehouseIndex: Map<String, Warehouse>
    ) {
        testPathExists()
        testNoPath()
        testSameWarehouse()
        testCycle()
        testLeastHopPath()
        testRealData(warehouseIndex)

        println("Routing validation completed.")
    }

    private fun testPathExists() {

        val startWarehouse = createWarehouse("TEST-001")
        val destinationWarehouse = createWarehouse("TEST-002")

        startWarehouse.addRoute(
            createRoute(
                "R1",
                startWarehouse,
                destinationWarehouse
            )
        )

        val path = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (
            path.map { it.id } ==
            listOf("TEST-001", "TEST-002")
        ) {
            println("Path exists: PASSED")
        } else {
            println("Path exists: FAILED")
        }
    }

    private fun testNoPath() {

        val startWarehouse = createWarehouse("TEST-001")
        val destinationWarehouse = createWarehouse("TEST-002")

        val path = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (path.isEmpty()) {
            println("No path: PASSED")
        } else {
            println("No path: FAILED")
        }
    }

    private fun testSameWarehouse() {

        val warehouse = createWarehouse("TEST-001")

        val path = router.findLeastHopPath(
            warehouse,
            warehouse
        )

        if (
            path.map { it.id } ==
            listOf("TEST-001")
        ) {
            println("Same warehouse: PASSED")
        } else {
            println("Same warehouse: FAILED")
        }
    }

    private fun testCycle() {

        val startWarehouse = createWarehouse("TEST-001")
        val middleWarehouse = createWarehouse("TEST-002")
        val destinationWarehouse = createWarehouse("TEST-003")

        startWarehouse.addRoute(
            createRoute(
                "R1",
                startWarehouse,
                middleWarehouse
            )
        )

        middleWarehouse.addRoute(
            createRoute(
                "R2",
                middleWarehouse,
                destinationWarehouse
            )
        )

        destinationWarehouse.addRoute(
            createRoute(
                "R3",
                destinationWarehouse,
                startWarehouse
            )
        )

        val path = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (
            path.map { it.id } ==
            listOf(
                "TEST-001",
                "TEST-002",
                "TEST-003"
            )
        ) {
            println("Cycle: PASSED")
        } else {
            println("Cycle: FAILED")
        }
    }

    private fun testLeastHopPath() {

        val startWarehouse = createWarehouse("WH-001")
        val shortPathWarehouse = createWarehouse("WH-002")
        val alternativePathWarehouse = createWarehouse("WH-003")
        val destinationWarehouse = createWarehouse("WH-004")
        val extraPathWarehouse = createWarehouse("WH-005")

        // Short path:
        // WH-001 → WH-002 → WH-004
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
        )// Longer path:
        // WH-001 → WH-003 → WH-005 → WH-004
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

        val path = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (
            path.map { it.id } ==
            listOf(
                "WH-001",
                "WH-002",
                "WH-004"
            )
        ) {
            println("Least-hop path: PASSED")
        } else {
            println("Least-hop path: FAILED")
        }
    }

    private fun testRealData(
        warehouseIndex: Map<String, Warehouse>
    ) {

        val startWarehouse =
            warehouseIndex["WH-001"]

        val destinationWarehouse =
            warehouseIndex["WH-002"]

        if (
            startWarehouse == null ||
            destinationWarehouse == null
        ) {
            println("Real data integration: FAILED")
            return
        }

        val path = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )

        if (
            path.map { it.id } ==
            listOf("WH-001", "WH-002")
        ) {
            println("Real data integration: PASSED")
        } else {
            println("Real data integration: FAILED")
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
}