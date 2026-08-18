
package validation

import domain.model.Warehouse
import domain.routing.BidirectionalRouter

class BidirectionalRoutingValidation(
    private val warehouseIndex: Map<String, Warehouse>,
    private val router: BidirectionalRouter
) {

    fun validateRouting() {
        validateSameWarehouse()
        validatePath()
        validateNoPath()
        validateShortestPath()
        validateBenchmark()

        println("Bidirectional routing validation completed.")
    }

    private fun validateSameWarehouse() {
        val warehouse = warehouseIndex.values.first()

        val path = router.findShortestPath(
            warehouse,
            warehouse
        )

        require(path.size == 1) {
            "Same warehouse test failed."
        }

        println("Same warehouse: PASS")
    }

    private fun validatePath() {
        val start = warehouseIndex.values.first()
        val destination = findFarthestWarehouse(start)
            ?: error("No reachable destination found.")

        val path = router.findShortestPath(
            start,
            destination
        )

        require(path.isNotEmpty()) {
            "Path was not found."
        }

        require(path.first().id == start.id)
        require(path.last().id == destination.id)

        for (i in 0 until path.lastIndex) {
            require(
                path[i].outgoingRoutes.any {
                    it.destinationWarehouse.id == path[i + 1].id
                }
            )
        }

        println("Path exists and is valid: PASS")
    }

    private fun validateNoPath() {
        val pair = findUnreachablePair()

        if (pair == null) {
            println("No path: SKIPPED")
            return
        }

        val path = router.findShortestPath(
            pair.first,
            pair.second
        )

        require(path.isEmpty()) {
            "Expected no path."
        }

        println("No path: PASS")
    }

    private fun validateShortestPath() {
        val start = warehouseIndex.values.first()

        val destination = findFarthestWarehouse(start)
            ?: error("No destination found.")

        val bidirectionalPath =
            router.findShortestPath(start, destination)

        val standardPath =
            runStandardBfs(start, destination)

        require(
            bidirectionalPath.size == standardPath.path.size
        ) {
            "Bidirectional BFS did not return the shortest path."
        }

        println("Shortest path: PASS")
    }

    private fun validateBenchmark() {
        val start = warehouseIndex.values.first()

        val destination = findFarthestWarehouse(start)
            ?: error("No destination found.")

        val standardStart = System.nanoTime()

        val standard = runStandardBfs(
            start,
            destination
        )

        val standardTime =
            System.nanoTime() - standardStart

        val bidirectionalStart = System.nanoTime()

        val bidirectionalPath =
            router.findShortestPath(
                start,
                destination
            )

        val bidirectionalTime =
            System.nanoTime() - bidirectionalStart

        require(
            standard.path.size == bidirectionalPath.size
        ) {
            "Benchmark paths are different."
        }

        println()
        println("========== Benchmark ==========")
        println("Start: ${start.id}")
        println("Destination: ${destination.id}")
        println(
            "Path length: ${bidirectionalPath.size - 1}"
        )
        println(
            "Standard BFS evaluated: " +
                    standard.evaluated
        )
        println(
            "Bidirectional BFS evaluated: " +
                    router.evaluatedWarehouses
        )
        println(
            "Standard BFS time: $standardTime ns"
        )
        println(
            "Bidirectional BFS time: $bidirectionalTime ns"
        )
        println("===============================")
        println("Benchmark: PASS")
    }


    private fun runStandardBfs(
        start: Warehouse,
        destination: Warehouse
    ): BfsResult {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()
        val parent = mutableMapOf<String, Warehouse>()

        queue.addLast(start)
        visited.add(start.id)

        var evaluated = 0

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            evaluated++

            if (current.id == destination.id) {
                break
            }

            for (route in current.outgoingRoutes) {
                val next = route.destinationWarehouse

                if (visited.add(next.id)) {
                    parent[next.id] = current
                    queue.addLast(next)
                }
            }
        }

        val path = mutableListOf<Warehouse>()
        var current = destination

        path.add(current)

        while (current.id in parent) {
            current = parent[current.id]!!
            path.add(current)
        }

        path.reverse()

        return BfsResult(path, evaluated)
    }

    private fun findFarthestWarehouse(
        start: Warehouse
    ): Warehouse? {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()

        queue.addLast(start)
        visited.add(start.id)

        var farthest: Warehouse? = null

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            farthest = current

            for (route in current.outgoingRoutes) {
                val next = route.destinationWarehouse

                if (visited.add(next.id)) {
                    queue.addLast(next)
                }
            }
        }

        return farthest
    }

    private fun findUnreachablePair():
            Pair<Warehouse, Warehouse>? {

        for (start in warehouseIndex.values) {
            val reachable = reachableIds(start)

            for (destination in warehouseIndex.values) {
                if (
                    start.id != destination.id &&
                    destination.id !in reachable
                ) {
                    return start to destination
                }
            }
        }

        return null
    }

    private fun reachableIds(
        start: Warehouse
    ): Set<String> {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()

        queue.addLast(start)
        visited.add(start.id)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()

            for (route in current.outgoingRoutes) {
                val next = route.destinationWarehouse

                if (visited.add(next.id)) {
                    queue.addLast(next)
                }
            }
        }

        return visited
    }

    private data class BfsResult(
        val path: List<Warehouse>,
        val evaluated: Int
    )
}
