package domain.routing

import domain.model.Warehouse

class BidirectionalRouter(private val warehouses: List<Warehouse>) {

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

        while (forwardQueue.isNotEmpty() && backwardQueue.isNotEmpty()) {

            val forwardMeetingWarehouse = performForwardSearchStep(
                forwardQueue = forwardQueue,
                forwardVisitedWarehouseIds = forwardVisitedWarehouseIds,
                forwardParentById = forwardParentById,
                backwardVisitedWarehouseIds = backwardVisitedWarehouseIds
            )

            if (forwardMeetingWarehouse != null) {
                return buildCompletePath(
                    meetingWarehouse = forwardMeetingWarehouse,
                    forwardParentById = forwardParentById,
                    backwardParentById = backwardParentById
                )
            }

            val backwardMeetingWarehouse = performBackwardSearchStep(
                backwardQueue = backwardQueue,
                backwardVisitedWarehouseIds = backwardVisitedWarehouseIds,
                backwardParentById = backwardParentById,
                forwardVisitedWarehouseIds = forwardVisitedWarehouseIds
            )

            if (backwardMeetingWarehouse != null) {
                return buildCompletePath(
                    meetingWarehouse = backwardMeetingWarehouse,
                    forwardParentById = forwardParentById,
                    backwardParentById = backwardParentById
                )
            }
        }
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

    private fun buildCompletePath(
        meetingWarehouse: Warehouse,
        forwardParentById: Map<String, Warehouse>,
        backwardParentById: Map<String, Warehouse>
    ): List<Warehouse> {

        val forwardPath = buildForwardPath(
            meetingWarehouse = meetingWarehouse,
            forwardParentById = forwardParentById
        )

        val backwardPath = buildBackwardPath(
            meetingWarehouse = meetingWarehouse,
            backwardParentById = backwardParentById
        )

        return forwardPath + backwardPath.drop(1)
    }


    private fun buildForwardPath(
        meetingWarehouse: Warehouse,
        forwardParentById: Map<String, Warehouse>
    ): List<Warehouse> {

        val forwardPath = mutableListOf<Warehouse>()
        var currentWarehouse = meetingWarehouse

        forwardPath.add(currentWarehouse)

        while (currentWarehouse.id in forwardParentById) {
            val parentWarehouse = forwardParentById[currentWarehouse.id]
                ?: break

            currentWarehouse = parentWarehouse
            forwardPath.add(currentWarehouse)
        }
        forwardPath.reverse()
        return forwardPath
    }

    private fun buildBackwardPath(
        meetingWarehouse: Warehouse,
        backwardParentById: Map<String, Warehouse>
    ): List<Warehouse> {

        val backwardPath = mutableListOf<Warehouse>()
        var currentWarehouse = meetingWarehouse

        backwardPath.add(currentWarehouse)

        while (currentWarehouse.id in backwardParentById) {
            val nextWarehouse = backwardParentById[currentWarehouse.id]
                ?: break

            currentWarehouse = nextWarehouse
            backwardPath.add(currentWarehouse)
        }
        return backwardPath
    }


}




