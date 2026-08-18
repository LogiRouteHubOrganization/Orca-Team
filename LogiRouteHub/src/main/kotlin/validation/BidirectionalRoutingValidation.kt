package validation

import domain.model.Warehouse
import domain.routing.BidirectionalRouter

class BidirectionalRoutingValidation(
    private val warehouseIndex: Map<String, Warehouse>,
    private val router: BidirectionalRouter
) {

    fun validateRouting() {
        validateSameWarehouse()
        validateDirectPath()
        validateValidPath()
        validateNoPath()
        validateShortestPath()
        validateBenchmark()
        println("Bidirectional routing validation completed.")
    }

    private fun validateSameWarehouse() {
        val warehouse = warehouseIndex.values.firstOrNull() ?: run {
                println("Same warehouse: SKIPPED")
                return
            }
        val  path = router.findShortestPath(
            warehouse,
            warehouse
        )
        require( path.size == 1) {
            "Same warehouse test failed."
        }
        require(path.first().id == warehouse.id) {
            "Same warehouse path contains an incorrect warehouse."
        }
        println("Same warehouse: PASS")
    }

    private fun validateDirectPath() {
        val startWarehouse = warehouseIndex.values.firstOrNull() ?: run {
                println("Direct path: SKIPPED")
                return
            }
        val destinationWarehouse = startWarehouse.outgoingRoutes.firstOrNull()
            ?.destinationWarehouse ?: run {
                println("Direct path: SKIPPED")
                return
            }
        val path = router.findShortestPath(
            startWarehouse,
            destinationWarehouse
        )
        require(path.size == 2) {
            "Direct path should contain exactly two warehouses."
        }
        require(path.first().id == startWarehouse.id) {
            "Direct path starts at the wrong warehouse."
        }
        require(path.last().id == destinationWarehouse.id) {
            "Direct path ends at the wrong warehouse."
        }
        println("Direct path: PASS")
    }

    private fun validateValidPath() {
        val startWarehouse = warehouseIndex.values.firstOrNull() ?: run {
                println("Valid path: SKIPPED")
                return
            }
        val destinationWarehouse = findLongestHopDestination(startWarehouse) ?: run {
                println("Valid path: SKIPPED")
                return
            }
        val path = router.findShortestPath(
            startWarehouse,
            destinationWarehouse
        )
        require(path.isNotEmpty()) {
            "Path was not found."
        }
        require(path.first().id == startWarehouse.id) {
            "Path starts at the wrong warehouse."
        }
        require(path.last().id == destinationWarehouse.id) {
            "Path ends at the wrong warehouse."
        }
        require(isValidPath(path)) {
            "Path contains an invalid warehouse transition."
        }
        println("Path exists and is valid: PASS")
    }

    private fun validateNoPath() {
        val unreachableWarehousePair  = findUnreachablePair()

        if (unreachableWarehousePair  == null) {
            println("No path: SKIPPED")
            return
        }
        val path = router.findShortestPath(
            unreachableWarehousePair .startWarehouse,
            unreachableWarehousePair .destinationWarehouse
        )
        require(path.isEmpty()) {
            "Expected no path between ${unreachableWarehousePair .startWarehouse.id} " +
             "and ${unreachableWarehousePair .destinationWarehouse.id}."
        }
        println("No path: PASS")
    }

    private fun validateShortestPath() {
        val startWarehouse = warehouseIndex.values.firstOrNull() ?: run {
                println("Shortest path: SKIPPED")
                return
            }
        val destinationWarehouse = findLongestHopDestination(startWarehouse) ?: run {
                println("Shortest path: SKIPPED")
                return
            }
        val bidirectionalPath =
            router.findShortestPath(startWarehouse, destinationWarehouse)

        val standardPath =
            runStandardBfs(startWarehouse, destinationWarehouse)

        val bidirectionalHops = bidirectionalPath.size - 1
        val standardHops = standardPath.path.size - 1

        require(bidirectionalHops == standardHops) {
            "Bidirectional BFS did not return the shortest path."
        }
        println("Shortest path: PASS")
    }

    private fun validateBenchmark() {
        val start = warehouseIndex.values.firstOrNull() ?: run {
                println("Benchmark: SKIPPED")
                return
            }
        val destination = findLongestHopDestination(start) ?: run {
                println("Benchmark: SKIPPED")
                return
            }
        val standardPath = runStandardBfs(start, destination)

        val bidirectionalPath =
            router.findShortestPath(
                start,
                destination
            )

        val standardHops = standardPath.path.size - 1
        val bidirectionalHops = bidirectionalPath.size - 1

        require(standardHops == bidirectionalHops) {
            "Benchmark paths have different lengths."
        }

        println()
        println("========== Bidirectional BFS Benchmark ==========")
        println("Start: ${start.id}")
        println("Destination: ${destination.id}")
        println("Path hops: $bidirectionalHops")

        println("Standard BFS evaluated: " +
                    standardPath.evaluatedWarehouses)

        println("Bidirectional BFS evaluated: "
                + router.evaluatedWarehouses)
        println("=================================================")
        println("Benchmark: PASS")
    }

    private fun runStandardBfs(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse): BfsResult {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()
        val parent = mutableMapOf<String, Warehouse>()

        queue.addLast(startWarehouse)
        visited.add(startWarehouse.id)

        var evaluatedWarehouses = 0

        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()
            evaluatedWarehouses++

            if (currentWarehouse.id == destinationWarehouse.id) {
                break
            }
            for (route in currentWarehouse.outgoingRoutes) {
                val nextWarehouse = route.destinationWarehouse

                if (visited.add(nextWarehouse.id)) {
                    parent[nextWarehouse.id] = currentWarehouse
                    queue.addLast(nextWarehouse)
                }
            }
        }

        if (destinationWarehouse.id !in visited) {
            return BfsResult(path = emptyList(),
                evaluatedWarehouses = evaluatedWarehouses)
        }
        val path = mutableListOf<Warehouse>()
        var currentWarehouse = destinationWarehouse

        path.add(currentWarehouse)

        while (currentWarehouse.id in parent) {
            val parentWarehouse = parent[currentWarehouse.id] ?: break
            currentWarehouse = parentWarehouse
            path.add(currentWarehouse)
        }
        path.reverse()

        return BfsResult(path = path,
            evaluatedWarehouses = evaluatedWarehouses)
    }

    private fun findLongestHopDestination(start: Warehouse): Warehouse? {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()

        queue.addLast(start)
        visited.add(start.id)

        var farthestWarehouse: Warehouse? = null

        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()

            if (currentWarehouse.id != start.id) {
                farthestWarehouse = currentWarehouse
            }

            for (route in currentWarehouse.outgoingRoutes) {
                val nextWarehouse = route.destinationWarehouse

                if (visited.add(nextWarehouse.id)) {
                    queue.addLast(nextWarehouse)
                }
            }
        }
        return farthestWarehouse
    }

    private fun findUnreachablePair(): WarehousePair? {

        for (startWarehouse in warehouseIndex.values) {
            val reachableIds = reachableIds(startWarehouse)

            for (destinationWarehouse in warehouseIndex.values) {
                if (startWarehouse.id != destinationWarehouse.id &&
                    destinationWarehouse.id !in reachableIds) {

                    return WarehousePair(startWarehouse= startWarehouse
                        , destinationWarehouse = destinationWarehouse)
                }
            }
        }
        return null
    }

    private fun reachableIds(startWarehouse: Warehouse): Set<String> {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()

        queue.addLast(startWarehouse)
        visited.add(startWarehouse.id)

        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()

            for (route in currentWarehouse.outgoingRoutes) {
                val nextWarehouse = route.destinationWarehouse

                if (visited.add(nextWarehouse.id)) {
                    queue.addLast(nextWarehouse)
                }
            }
        }
        return visited
    }

    private fun isValidPath(path: List<Warehouse>): Boolean {

        for (index in 0 until path.lastIndex) {
            val currentWarehouse = path[index]
            val nextWarehouse = path[index + 1]

            val hasValidRoute = currentWarehouse.outgoingRoutes.any { route ->
                route.destinationWarehouse.id == nextWarehouse.id
            }
            if (!hasValidRoute) {
                return false
            }
        }
        return true
    }

    private data class BfsResult(
        val path: List<Warehouse>,
        val evaluatedWarehouses: Int
    )

    private data class WarehousePair(
        val startWarehouse: Warehouse,
        val destinationWarehouse: Warehouse
    )
}