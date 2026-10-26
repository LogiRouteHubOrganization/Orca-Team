package domain.usecase

import domain.model.Warehouse
import kotlin.math.hypot

class OptimizeRoute2OptUseCase {

    private fun calculateDistance(p1: Warehouse, p2: Warehouse): Double {
        return hypot(p1.latitude - p2.latitude, p1.longitude - p2.longitude)
    }

    private fun calculateTotalDistance(route: List<Warehouse>): Double {
        var total = 0.0
        for (i in 0 until route.size - 1) {
            total += calculateDistance(route[i], route[i + 1])
        }
        return total
    }

    operator fun invoke(
        originWarehouse: Warehouse,
        deliveryStops: List<Warehouse>
    ): Pair<List<Warehouse>, Map<String, Double>> {

        require(deliveryStops.size == 15) { "Delivery stops must contain exactly 15 coordinates." }

        // 1. Tour Initialization: Origin Warehouse -> 15 Stops -> Return to Origin Warehouse
        var currentRoute = mutableListOf<Warehouse>().apply {
            add(originWarehouse)
            addAll(deliveryStops)
            add(originWarehouse)
        }

        val initialDistance = calculateTotalDistance(currentRoute)
        var improved = true
        val maxIterations = 1000
        var iterations = 0

        while (improved && iterations < maxIterations) {
            improved = false
            iterations++

            for (i in 1 until currentRoute.size - 2) {
                for (j in i + 1 until currentRoute.size - 1) {
                    val candidateRoute = currentRoute.toMutableList()
                    candidateRoute.subList(i, j + 1).reverse()

                    val candidateDistance = calculateTotalDistance(candidateRoute)
                    if (candidateDistance < calculateTotalDistance(currentRoute)) {
                        currentRoute = candidateRoute
                        improved = true
                    }
                }
            }
        }

        val finalDistance = calculateTotalDistance(currentRoute)
        val distanceSaved = initialDistance - finalDistance
        val savingsPercentage = if (initialDistance > 0.0) (distanceSaved / initialDistance) * 100.0 else 0.0

        // 5. Route Distance & Fuel Savings Verification
        val metrics = mapOf(
            "initialDistance" to initialDistance,
            "finalDistance" to finalDistance,
            "distanceSaved" to distanceSaved,
            "savingsPercentage" to savingsPercentage
        )

        println("=== 2-Opt Route Optimization Summary ===")
        println("Initial Route Distance: $initialDistance")
        println("Optimized Route Distance: $finalDistance")
        println("Distance & Fuel Saved: $distanceSaved ($savingsPercentage%)")

        return Pair(currentRoute, metrics)
    }
}