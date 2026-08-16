package domain.routing

import domain.model.Warehouse
import domain.model.Route
class ShortestPathRouter(private val warehouses: List<Warehouse>) {

    fun findShortestPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {

        val distanceByWarehouse = mutableMapOf<Warehouse, Double>()
        val visitedWarehouses = mutableSetOf<Warehouse>()
        val previousWarehouse = mutableMapOf<Warehouse, Warehouse?>()

        for (warehouse in warehouses) {
            distanceByWarehouse[warehouse] = Double.POSITIVE_INFINITY

            previousWarehouse[warehouse] = null
        }
        distanceByWarehouse[startWarehouse] = 0.0
        return emptyList()
    }

    private fun getUnvisitedNodeWithMinDistance(
        distanceByWarehouse: Map<Warehouse, Double>,
        visitedWarehouses: Set<Warehouse>
    ): Warehouse? {

        var selectedWarehouse: Warehouse? = null
        var minimumDistance = Double.POSITIVE_INFINITY

        for ((warehouse, distance) in distanceByWarehouse) {

            if (warehouse in visitedWarehouses) {
                continue
            }
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

        val currentDistance = distanceByWarehouse[currentWarehouse] ?: 0.0
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