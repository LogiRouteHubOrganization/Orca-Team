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

        val queue = ArrayDeque<Warehouse>()
        val visitedWarehouses = mutableSetOf<String>()

        queue.addLast(startWarehouse)
        visitedWarehouses.add(startWarehouse.id)

        while (queue.isNotEmpty()) {

            val currentWarehouse = queue.removeFirst()

            for (route in currentWarehouse.outgoingRoutes) {

                val nextWarehouse = route.destinationWarehouse

                if (nextWarehouse.id in visitedWarehouses) {
                    continue
                }

                if (nextWarehouse.id == destinationWarehouse.id) {
                    return listOf(nextWarehouse)
                }

                visitedWarehouses.add(nextWarehouse.id)
                queue.addLast(nextWarehouse)
            }
        }

        return emptyList()
    }
}