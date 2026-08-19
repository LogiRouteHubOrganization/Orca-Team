package validation

import domain.model.Warehouse
import domain.routing.BidirectionalRouter

private const val SINGLE_STEP = 1
private const val DIRECT_PATH_WAREHOUSE_COUNT = 2
private const val INITIAL_EVALUATED_WAREHOUSE_COUNT = 0
private const val NO_EVALUATIONS = 0
private const val ZERO_PERCENTAGE = 0.0
private const val PERCENTAGE_MULTIPLIER = 100.0
private const val NANOSECONDS_PER_MILLISECOND = 1_000_000.0

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
        val path = router.findLeastHopPath(
            warehouse,
            warehouse
        )
        require(path.size == SINGLE_STEP) {
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
        val path = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )
        require(path.size == DIRECT_PATH_WAREHOUSE_COUNT) {
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
        val path = router.findLeastHopPath(
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
        val unreachableWarehousePair = findUnreachablePair()

        if (unreachableWarehousePair == null) {
            println("No path: SKIPPED")
            return
        }
        val path = router.findLeastHopPath(
            unreachableWarehousePair.startWarehouse,
            unreachableWarehousePair.destinationWarehouse
        )
        require(path.isEmpty()) {
            "Expected no path between ${unreachableWarehousePair.startWarehouse.id} " +
                    "and ${unreachableWarehousePair.destinationWarehouse.id}."
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
        val bidirectionalPath = router.findLeastHopPath(startWarehouse, destinationWarehouse)

        val standardPath = runStandardBfs(startWarehouse, destinationWarehouse)

        val bidirectionalHops = bidirectionalPath.size - SINGLE_STEP
        val standardHops = standardPath.path.size - SINGLE_STEP

        require(bidirectionalHops == standardHops) {
            "Bidirectional BFS did not return the shortest path."
        }
        println("Shortest path: PASS")
    }

    private fun validateBenchmark() {

        val startWarehouse = warehouseIndex.values.firstOrNull() ?: run {
            println("Benchmark: SKIPPED")
            return
        }
        val destinationWarehouse = findLongestHopDestination(startWarehouse) ?: run {
            println("Benchmark: SKIPPED")
            return
        }
        val standardStartTime = System.nanoTime()
        val standardPath = runStandardBfs(startWarehouse, destinationWarehouse)
        val standardDurationMs = (System.nanoTime() - standardStartTime) / NANOSECONDS_PER_MILLISECOND

        val bidirectionalStartTime = System.nanoTime()
        val bidirectionalPath = router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )
        val bidirectionalDurationMs = (System.nanoTime() - bidirectionalStartTime) / NANOSECONDS_PER_MILLISECOND
        val standardHops = standardPath.path.size - SINGLE_STEP
        val bidirectionalHops = bidirectionalPath.size - SINGLE_STEP

        require(standardHops == bidirectionalHops) {
            "Benchmark paths have different lengths."
        }
        printBenchmarkResult(
            startWarehouse,
            destinationWarehouse,
            standardPath,
            bidirectionalPath,
            standardHops,
            bidirectionalHops,
            standardDurationMs,
            bidirectionalDurationMs
        )
    }

    private fun printBenchmarkResult(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        standardPath: BfsResult,
        bidirectionalPath: List<Warehouse>,
        standardHops: Int,
        bidirectionalHops: Int,
        standardDurationMs: Double,
        bidirectionalDurationMs: Double
    ) {
        val standardPathString = standardPath.path.joinToString(" -> ") { it.id }
        val bidirectionalPathString = bidirectionalPath.joinToString(" -> ") { it.id }

        val evaluatedDifference = standardPath.evaluatedWarehouses - router.evaluatedWarehouseCount
        val improvementPercentage = if (standardPath.evaluatedWarehouses > NO_EVALUATIONS) {
            evaluatedDifference * PERCENTAGE_MULTIPLIER / standardPath.evaluatedWarehouses
        } else ZERO_PERCENTAGE

        printBenchmarkComparison(
            startWarehouse,
            destinationWarehouse,
            standardPathString,
            bidirectionalPathString,
            standardHops,
            bidirectionalHops,
            standardPath.evaluatedWarehouses,
            standardDurationMs,
            bidirectionalDurationMs
        )
        printPerformanceSummary(
            improvementPercentage,
            evaluatedDifference
        )
    }

    private fun printBenchmarkComparison(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        standardPathString: String,
        bidirectionalPathString: String,
        standardHops: Int,
        bidirectionalHops: Int,
        standardEvaluatedWarehouseCount: Int,
        standardDurationMs: Double,
        bidirectionalDurationMs: Double
    ) {
        println()
        println("==================================================================")
        println("                   BFS ALGORITHM BENCHMARK")
        println("==================================================================")
        println(
            String.format("%-18s : %s -> %s", "Route", startWarehouse.id, destinationWarehouse.id)
        )
        println(
            String.format("%-18s : %d", "Target Path Hops", bidirectionalHops)
        )
        println("------------------------------------------------------------------")
        println("\n[ UNIDIRECTIONAL BFS ]")
        println(String.format("  %-16s : %s", "Path", standardPathString))
        println(String.format("  %-16s : %d hops", "Path Length", standardHops))
        println(
            String.format("  %-16s : %d warehouses", "Evaluated", standardEvaluatedWarehouseCount)
        )
        println(
            String.format("  %-16s : %.3f ms", "Execution Time", standardDurationMs)
        )
        println("\n[ BIDIRECTIONAL BFS ]")
        println(String.format("  %-16s : %s", "Path", bidirectionalPathString))
        println(String.format("  %-16s : %d hops", "Path Length", bidirectionalHops))
        println(String.format("  %-16s : %d warehouses", "Evaluated", router.evaluatedWarehouseCount))
        println(String.format("  %-16s : %.3f ms", "Execution Time", bidirectionalDurationMs))
    }

    private fun printPerformanceSummary(
        improvementPercentage: Double,
        evaluatedDifference: Int
    ) {
        println("\n------------------------------------------------------------------")
        println(" PERFORMANCE SUMMARY")
        println("------------------------------------------------------------------")
        println(
            String.format(
                "  %-20s : %.1f%% fewer warehouses evaluated", "Efficiency Gain", improvementPercentage
            )
        )
        println(
            String.format(
                "  %-20s : %d warehouses", "Evaluations Saved", evaluatedDifference
            )
        )
        println(
            String.format("  %-20s : PASSED", "Validation Check")
        )
        println("==================================================================")
        println()
    }

    private fun runStandardBfs(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): BfsResult {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<String>()
        val parent = mutableMapOf<String, Warehouse>()
        queue.addLast(startWarehouse)
        visited.add(startWarehouse.id)
        var evaluatedWarehouses = INITIAL_EVALUATED_WAREHOUSE_COUNT

        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()
            evaluatedWarehouses++

            if (currentWarehouse.id == destinationWarehouse.id) break
            for (route in currentWarehouse.outgoingRoutes) {
                val nextWarehouse = route.destinationWarehouse

                if (visited.add(nextWarehouse.id)) {
                    parent[nextWarehouse.id] = currentWarehouse
                    queue.addLast(nextWarehouse)
                }
            }
        }
        if (destinationWarehouse.id !in visited) {
            return BfsResult(path = emptyList(), evaluatedWarehouses = evaluatedWarehouses)
        }
        val path = buildStandardBfsPath(destinationWarehouse, parent)
        return BfsResult(path = path, evaluatedWarehouses = evaluatedWarehouses)
    }

    private fun buildStandardBfsPath(
        destinationWarehouse: Warehouse,
        parentById: Map<String, Warehouse>
    ): List<Warehouse> {

        val path = mutableListOf<Warehouse>()
        var currentWarehouse = destinationWarehouse

        path.add(currentWarehouse)

        while (currentWarehouse.id in parentById) {
            val parentWarehouse =
                parentById[currentWarehouse.id]
                    ?: break

            currentWarehouse = parentWarehouse
            path.add(currentWarehouse)
        }
        path.reverse()
        return path
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
                    destinationWarehouse.id !in reachableIds
                ) {
                    return WarehousePair(
                        startWarehouse = startWarehouse, destinationWarehouse = destinationWarehouse
                    )
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
            val nextWarehouse = path[index + SINGLE_STEP]

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
