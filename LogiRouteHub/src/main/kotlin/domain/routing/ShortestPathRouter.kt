package domain.routing

import domain.model.Warehouse

class ShortestPathRouter(private val warehouses: Collection<Warehouse>) {

    fun findShortestPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {

        val shortestDistances = mutableMapOf<Warehouse, Double>()
        val visitedWarehouses = mutableSetOf<Warehouse>()
        val previousWarehouse  = mutableMapOf<Warehouse, Warehouse?>()


        for (warehouse in warehouses) {
            shortestDistances[warehouse] = Double.POSITIVE_INFINITY
            previousWarehouse[warehouse] = null
        }

        shortestDistances[startWarehouse] = 0.0
        return emptyList()
    }
}