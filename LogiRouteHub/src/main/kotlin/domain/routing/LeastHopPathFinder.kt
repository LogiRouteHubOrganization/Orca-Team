package domain.routing

import domain.model.Warehouse

interface LeastHopPathFinder {
    fun findLeastHopPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse>
}
