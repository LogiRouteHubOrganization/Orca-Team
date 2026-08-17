package domain.routing

import domain.model.Warehouse

class BidirectionalRouter(
    private val warehouses: List<Warehouse>
) {
    fun findShortestPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {
        if (startWarehouse.id == destinationWarehouse.id) {
            return listOf(startWarehouse)
        }

        val forwardQueue = ArrayDeque<Warehouse>()
        val backwardQueue = ArrayDeque<Warehouse>()
        val forwardVisitedWarehouseIds = mutableSetOf<String>()
        val backwardVisitedWarehouseIds = mutableSetOf<String>()
        val forwardParentById = mutableMapOf<String, Warehouse>()
        val backwardParentById = mutableMapOf<String, Warehouse>()

        forwardQueue.addLast(startWarehouse)

        forwardVisitedWarehouseIds.add(startWarehouse.id)

        backwardQueue.addLast(destinationWarehouse)

        backwardVisitedWarehouseIds.add(destinationWarehouse.id)

        performForwardSearchStep(
            forwardQueue = forwardQueue,
            forwardVisitedWarehouseIds = forwardVisitedWarehouseIds,
            forwardParentById = forwardParentById,
            backwardVisitedWarehouseIds = backwardVisitedWarehouseIds
        )

        return emptyList()
    }

    private fun performForwardSearchStep(
        forwardQueue: ArrayDeque<Warehouse>,
        forwardVisitedWarehouseIds: MutableSet<String>,
        forwardParentById: MutableMap<String, Warehouse>,
        backwardVisitedWarehouseIds: Set<String>
    ): Warehouse? {
        val currentWarehouse = forwardQueue.removeFirst()

        for (route in currentWarehouse.outgoingRoutes) {
            val nextWarehouse = route.destinationWarehouse

            if (nextWarehouse.id in forwardVisitedWarehouseIds) continue

            forwardVisitedWarehouseIds.add(nextWarehouse.id)

            forwardParentById[nextWarehouse.id] = currentWarehouse

            if (nextWarehouse.id in backwardVisitedWarehouseIds) return nextWarehouse

            forwardQueue.addLast(nextWarehouse)
        }

        return null
    }

    private fun performBackwardSearchStep(
        backwardQueue: ArrayDeque<Warehouse>,
        backwardVisitedWarehouseIds: MutableSet<String>,
        backwardParentById: MutableMap<String, Warehouse>,
        forwardVisitedWarehouseIds: Set<String>
    ): Warehouse? {
        val currentWarehouse = backwardQueue.removeFirst()

        for (warehouse in warehouses) {
            for (route in warehouse.outgoingRoutes) {
                if (route.destinationWarehouse.id != currentWarehouse.id) continue

                val previousWarehouse = warehouse

                if (previousWarehouse.id in backwardVisitedWarehouseIds) continue

                backwardVisitedWarehouseIds.add(previousWarehouse.id)

                backwardParentById[previousWarehouse.id] = currentWarehouse

                if (previousWarehouse.id in forwardVisitedWarehouseIds) {
                    return previousWarehouse
                }

                backwardQueue.addLast(previousWarehouse)
            }
        }

        return null
    }
}
//كملوا بنفس اسماء هاي الدوال حسب مهمتكم وبس تخلصوا احذفوا الكومنت
// performBackwardSearchStep()
// findMeetingWarehouse()
// reconstructPath
