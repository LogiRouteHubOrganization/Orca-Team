package domain.routing

import domain.model.Warehouse

class LeastHopRouter {

    fun findLeastHopPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {

        if (startWarehouse.id == destinationWarehouse.id) {
            return listOf(startWarehouse)
        }
        return emptyList()
    }
}