package domain.routing

import domain.model.Warehouse

interface ShortestPathFinder {

    fun findShortestPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse>
}
