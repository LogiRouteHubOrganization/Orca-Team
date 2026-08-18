package domain.routing

import domain.model.Route
import domain.model.Warehouse

/**
 * Dijkstra's algorithm finds the shortest path by evaluating the
 * cumulative distance of all routes.
 *
 * BFS finds the path with the fewest hops between warehouses.
 * However, it does not consider physical route distances.
 *
 * When route distances vary, BFS may select a path with fewer
 * intermediate warehouses but a longer total distance.
 *
 * Dijkstra evaluates the total route cost and selects the path
 * with the minimum cumulative distance.
 *
 *  For example, BFS may choose a 2-hop path with a total distance
 *  of 14 km over a 3-hop path with a total distance of 9 km.
 *  Dijkstra correctly selects the 9 km path because it minimizes
 *  the cumulative route distance.
 *
 *  Therefore, BFS is suitable for finding the fewest hops, while
 *  Dijkstra is required when the goal is to minimize physical
 *  distance in a weighted graph.
 */

class ShortestPathRouter(
    private val warehouses: List<Warehouse>
) {

    fun findShortestPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {
        if (startWarehouse.id == destinationWarehouse.id) return listOf(startWarehouse)
        val distanceByWarehouse = mutableMapOf<Warehouse, Double>()
        val visitedWarehouses = mutableSetOf<Warehouse>()
        val previousWarehouseMap = mutableMapOf<Warehouse, Warehouse?>()
        for (warehouse in warehouses) {
            distanceByWarehouse[warehouse] = Double.POSITIVE_INFINITY
            previousWarehouseMap[warehouse] = null
        }
        distanceByWarehouse[startWarehouse] = 0.0
        while (true) {
            val currentWarehouse = getUnvisitedNodeWithMinDistance(
                distanceByWarehouse,
                visitedWarehouses
            ) ?: break
            if (currentWarehouse == destinationWarehouse) {
                return reconstructPath(
                    startWarehouse,
                    destinationWarehouse,
                    previousWarehouseMap
                )
            }
            visitedWarehouses.add(currentWarehouse)
            for (route in currentWarehouse.outgoingRoutes) {
                updateDistanceThroughRoute(
                    currentWarehouse,
                    route,
                    distanceByWarehouse,
                    previousWarehouseMap
                )
            }
        }
        return reconstructPath(
            startWarehouse,
            destinationWarehouse,
            previousWarehouseMap
        )
    }

    private fun getUnvisitedNodeWithMinDistance(
        distanceByWarehouse: Map<Warehouse, Double>,
        visitedWarehouses: Set<Warehouse>
    ): Warehouse? {
        var selectedWarehouse: Warehouse? = null
        var minimumDistance = Double.POSITIVE_INFINITY

        for ((warehouse, distance) in distanceByWarehouse) {

            if (warehouse in visitedWarehouses) continue
            if (distance < minimumDistance) {
                minimumDistance = distance
                selectedWarehouse = warehouse
            }
        }
        return selectedWarehouse
    }

    private fun updateDistanceThroughRoute(
        currentWarehouse: Warehouse,
        route: Route,
        distanceByWarehouse: MutableMap<Warehouse, Double>,
        previousWarehouse: MutableMap<Warehouse, Warehouse?>
    ) {
        val nextWarehouse = route.destinationWarehouse
        val currentDistance = distanceByWarehouse[currentWarehouse] ?: Double.POSITIVE_INFINITY
        val newDistance = currentDistance + route.distanceKm

        if (newDistance < (distanceByWarehouse[nextWarehouse] ?: Double.POSITIVE_INFINITY)) {
            distanceByWarehouse[nextWarehouse] = newDistance
            previousWarehouse[nextWarehouse] = currentWarehouse
        }
    }

    private fun reconstructPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        previousWarehouse: Map<Warehouse, Warehouse?>
    ): List<Warehouse> {
        val path = mutableListOf<Warehouse>()
        var currentWarehouse: Warehouse? = destinationWarehouse

        while (currentWarehouse != null) {
            path.add(currentWarehouse)

            if (currentWarehouse == startWarehouse) {
                path.reverse()
                return path
            }
            currentWarehouse = previousWarehouse[currentWarehouse]
        }
        return emptyList()
    }
}

