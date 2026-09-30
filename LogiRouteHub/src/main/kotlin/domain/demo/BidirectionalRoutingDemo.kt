package domain.demo

import domain.model.Warehouse
import domain.routing.BidirectionalRouter
import java.util.Locale

class BidirectionalRoutingDemo(
    private val warehouseIndex: Map<String, Warehouse>,
    private val router: BidirectionalRouter
) {

    private val singleStep = 1
    private val directPathWarehouseCount = 2
    private val initialEvaluatedWarehouseCount = 0
    private val noEvaluations = 0
    private val zeroPercentage = 0.0
    private val percentageMultiplier = 100.0
    private val nanosecondsPerMillisecond = 1_000_000.0

    fun runDemo() {
        demonstrateSameWarehouse()
        demonstrateDirectPath()
        demonstrateValidPath()
        demonstrateNoPath()
        demonstrateShortestPath()
        demonstrateBenchmark()

        println("Bidirectional routing demo completed.")
    }

    private fun demonstrateSameWarehouse() {
        val warehouse = warehouseIndex.values.firstOrNull() ?: run {
            println("Same warehouse: SKIPPED")
            return
        }

        val path = router.findLeastHopPath(warehouse, warehouse)

        if (path.size != singleStep) {
            println("Same warehouse: FAILED\nExpected path to contain one warehouse.")
            return
        }

        if (path.first().id != warehouse.id) {
            println("Same warehouse: FAILED\nSame warehouse path contains an incorrect warehouse.")
            return
        }

        println("Same warehouse: PASS")
    }

    private fun demonstrateDirectPath() {
        val startWarehouse = warehouseIndex.values.firstOrNull() ?: run {
            println("Direct path: SKIPPED")
            return
        }

        val destinationWarehouse = startWarehouse.outgoingRoutes.firstOrNull()
            ?.destinationWarehouse
            ?: run {
                println("Direct path: SKIPPED")
                return
            }

        val path = router.findLeastHopPath(startWarehouse, destinationWarehouse)

        if (path.size != directPathWarehouseCount) {
            println("Direct path: FAILED\nDirect path should contain exactly two warehouses.")
            return
        }

        if (path.first().id != startWarehouse.id || path.last().id != destinationWarehouse.id) {
            println("Direct path: FAILED\nDirect path starts or ends at the wrong warehouse.")
            return
        }

        println("Direct path: PASS")
    }

    private fun demonstrateValidPath() {
        val startWarehouse = warehouseIndex.values.firstOrNull() ?: run {
            println("Valid path: SKIPPED")
            return
        }

        val destinationWarehouse = findLongestHopDestination(startWarehouse) ?: run {
            println("Valid path: SKIPPED")
            return
        }

        val path = router.findLeastHopPath(startWarehouse, destinationWarehouse)

        if (path.isEmpty() || path.first().id != startWarehouse.id || path.last().id != destinationWarehouse.id) {
            println("Valid path: FAILED\nInvalid path boundaries.")
            return
        }

        for (index in 0 until path.lastIndex) {
            val currentWarehouse = path[index]
            val nextWarehouse = path[index + singleStep]

            val hasValidRoute = currentWarehouse.outgoingRoutes.any { route ->
                route.destinationWarehouse.id == nextWarehouse.id
            }

            if (!hasValidRoute) {
                println("Valid path: FAILED\nPath contains an invalid warehouse transition.")
                return
            }
        }

        println("Path exists and is valid: PASS")
    }

    private fun demonstrateNoPath() {
        val unreachablePair = findUnreachablePair() ?: run {
            println("No path: SKIPPED")
            return
        }

        val path = router.findLeastHopPath(
            unreachablePair.startWarehouse,
            unreachablePair.destinationWarehouse
        )

        if (path.isNotEmpty()) {
            println("No path: FAILED\nExpected no path between ${unreachablePair.startWarehouse.id} and ${unreachablePair.destinationWarehouse.id}")
            return
        }

        println("No path: PASS")
    }

    private fun demonstrateShortestPath() {
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

        val bidirectionalHops = bidirectionalPath.size - singleStep
        val standardHops = standardPath.path.size - singleStep

        if (bidirectionalHops != standardHops) {
            println("Shortest path: FAILED\nBidirectional BFS did not return the shortest path.")
            return
        }

        println("Shortest path: PASS")
    }

    private fun demonstrateBenchmark() {
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
        val standardDurationMs = (System.nanoTime() - standardStartTime) / nanosecondsPerMillisecond

        val bidirectionalStartTime = System.nanoTime()
        val bidirectionalPath = router.findLeastHopPath(startWarehouse, destinationWarehouse)
        val bidirectionalDurationMs = (System.nanoTime() - bidirectionalStartTime) / nanosecondsPerMillisecond

        val standardHops = standardPath.path.size - singleStep
        val bidirectionalHops = bidirectionalPath.size - singleStep

        if (standardHops != bidirectionalHops) {
            println("Benchmark: FAILED\nBenchmark paths have different lengths.")
            return
        }

        val benchmarkData = BenchmarkReportData(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            standardPath = standardPath,
            bidirectionalPath = bidirectionalPath,
            standardHops = standardHops,
            bidirectionalHops = bidirectionalHops,
            standardDurationMs = standardDurationMs,
            bidirectionalDurationMs = bidirectionalDurationMs
        )

        printBenchmarkComparison(benchmarkData)
    }

    private fun printBenchmarkComparison(benchmarkData: BenchmarkReportData) {
        val standardPathString = benchmarkData.standardPath.path.joinToString(" -> ") { it.id }
        val bidirectionalPathString = benchmarkData.bidirectionalPath.joinToString(" -> ") { it.id }

        val evaluatedDifference = benchmarkData.standardPath.evaluatedWarehouses - router.evaluatedWarehouseCount
        val improvementPercentage = if (benchmarkData.standardPath.evaluatedWarehouses > noEvaluations) {
            evaluatedDifference * percentageMultiplier / benchmarkData.standardPath.evaluatedWarehouses
        } else {
            zeroPercentage
        }

        printBenchmarkHeader(benchmarkData)
        printUnidirectionalSection(benchmarkData, standardPathString)
        printBidirectionalSection(benchmarkData, bidirectionalPathString)
        printPerformanceSummary(improvementPercentage, evaluatedDifference)
    }

    private fun printBenchmarkHeader(data: BenchmarkReportData) {
        println("\n==================================================================")
        println("                   BFS ALGORITHM BENCHMARK")
        println("==================================================================")
        println(String.format(Locale.US, "%-18s : %s -> %s", "Route", data.startWarehouse.id, data.destinationWarehouse.id))
        println(String.format(Locale.US, "%-18s : %d", "Target Path Hops", data.bidirectionalHops))
        println("------------------------------------------------------------------")
    }

    private fun printUnidirectionalSection(data: BenchmarkReportData, pathString: String) {
        println("\n[ UNIDIRECTIONAL BFS ]")
        println(String.format(Locale.US, "  %-16s : %s", "Path", pathString))
        println(String.format(Locale.US, "  %-16s : %d hops", "Path Length", data.standardHops))
        println(String.format(Locale.US, "  %-16s : %d warehouses", "Evaluated", data.standardPath.evaluatedWarehouses))
        println(String.format(Locale.US, "  %-16s : %.3f ms", "Execution Time", data.standardDurationMs))
    }

    private fun printBidirectionalSection(data: BenchmarkReportData, pathString: String) {
        println("\n[ BIDIRECTIONAL BFS ]")
        println(String.format(Locale.US, "  %-16s : %s", "Path", pathString))
        println(String.format(Locale.US, "  %-16s : %d hops", "Path Length", data.bidirectionalHops))
        println(String.format(Locale.US, "  %-16s : %d warehouses", "Evaluated", router.evaluatedWarehouseCount))
        println(String.format(Locale.US, "  %-16s : %.3f ms", "Execution Time", data.bidirectionalDurationMs))
    }

    private fun printPerformanceSummary(
        improvementPercentage: Double,
        evaluatedDifference: Int
    ) {
        println("\n------------------------------------------------------------------")
        println(" PERFORMANCE SUMMARY")
        println("------------------------------------------------------------------")
        println(String.format(Locale.US, "  %-20s : %.1f%% fewer warehouses evaluated", "Efficiency Gain", improvementPercentage))
        println(String.format(Locale.US, "  %-20s : %d warehouses", "Evaluations Saved", evaluatedDifference))
        println(String.format(Locale.US, "  %-20s : PASSED", "Demo Check"))
        println("==================================================================\n")
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

        var evaluatedWarehouses = initialEvaluatedWarehouseCount

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
            return BfsResult(emptyList(), evaluatedWarehouses)
        }

        val path = buildStandardBfsPath(destinationWarehouse, parent)
        return BfsResult(path, evaluatedWarehouses)
    }

    private fun buildStandardBfsPath(
        destinationWarehouse: Warehouse,
        parentById: Map<String, Warehouse>
    ): List<Warehouse> {
        val path = mutableListOf<Warehouse>()
        var currentWarehouse = destinationWarehouse
        path.add(currentWarehouse)

        while (currentWarehouse.id in parentById) {
            val parentWarehouse = parentById[currentWarehouse.id] ?: break
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

            for (destinationWarehouse in warehouseIndex.values) {
                if (startWarehouse.id != destinationWarehouse.id && destinationWarehouse.id !in visited) {
                    return WarehousePair(startWarehouse, destinationWarehouse)
                }
            }
        }

        return null
    }

    private data class BfsResult(
        val path: List<Warehouse>,
        val evaluatedWarehouses: Int
    )

    private data class WarehousePair(
        val startWarehouse: Warehouse,
        val destinationWarehouse: Warehouse
    )

    private data class BenchmarkReportData(
        val startWarehouse: Warehouse,
        val destinationWarehouse: Warehouse,
        val standardPath: BfsResult,
        val bidirectionalPath: List<Warehouse>,
        val standardHops: Int,
        val bidirectionalHops: Int,
        val standardDurationMs: Double,
        val bidirectionalDurationMs: Double
    )
}

