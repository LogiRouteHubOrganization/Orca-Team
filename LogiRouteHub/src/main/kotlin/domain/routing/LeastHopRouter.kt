package domain.routing

import domain.model.Warehouse

class LeastHopRouter : LeastHopPathFinder {

   override fun findLeastHopPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {

        if (startWarehouse.id == destinationWarehouse.id) return listOf(startWarehouse)
        val queue = ArrayDeque<Warehouse>()
        val visitedWarehouseIds = mutableSetOf<String>()
        val previousWarehouseById = mutableMapOf<String, Warehouse>()
        queue.addLast(startWarehouse)
        visitedWarehouseIds.add(startWarehouse.id)

        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()
            for (route in currentWarehouse.outgoingRoutes) {

                val nextWarehouse = route.destinationWarehouse
                if (nextWarehouse.id in visitedWarehouseIds) continue
                visitedWarehouseIds.add(nextWarehouse.id)
                previousWarehouseById[nextWarehouse.id] = currentWarehouse

                if (nextWarehouse.id == destinationWarehouse.id) {
                    return buildWarehousePath(
                        startWarehouse,
                        destinationWarehouse,
                        previousWarehouseById
                    )
                }
                queue.addLast(nextWarehouse)
            }
        }
        return emptyList()
    }

    private fun buildWarehousePath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        previousWarehouseById: Map<String, Warehouse>
    ): List<Warehouse> {

        val warehousePath = mutableListOf<Warehouse>()
        var currentWarehouse = destinationWarehouse

        warehousePath.add(currentWarehouse)

        while (currentWarehouse.id != startWarehouse.id) {
            val previousWarehouse = previousWarehouseById[currentWarehouse.id]
                ?: return emptyList()

            currentWarehouse = previousWarehouse
            warehousePath.add(currentWarehouse)
        }
        warehousePath.reverse()
        return warehousePath
    }
}
