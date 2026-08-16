package domain.routing

import domain.model.Warehouse

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
}