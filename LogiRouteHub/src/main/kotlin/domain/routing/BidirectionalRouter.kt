package domain.routing

import domain.model.Warehouse


class BidirectionalRouter(private val warehouses: List<Warehouse>) {
    var evaluatedWarehouseCount = 0
    private val duplicatedMeetingWarehouseCount = 1

    fun findLeastHopPath(startWarehouse: Warehouse, destinationWarehouse: Warehouse): List<Warehouse> {

        evaluatedWarehouseCount = 0
        if (startWarehouse.id == destinationWarehouse.id) return listOf(startWarehouse)
        val forwardQueue = initializeForwardQueue(startWarehouse)
        val backwardQueue = initializeBackwardQueue(destinationWarehouse)
        val forwardVisitedWarehouseIds = initializeVisitedWarehouseIds(startWarehouse)
        val backwardVisitedWarehouseIds = initializeVisitedWarehouseIds(destinationWarehouse)
        val forwardParentById = mutableMapOf<String, Warehouse>()
        val backwardParentById = mutableMapOf<String, Warehouse>()
        while (forwardQueue.isNotEmpty() && backwardQueue.isNotEmpty()) {
            val forwardMeetingWarehouse = performForwardSearchStep(
                forwardQueue,
                forwardVisitedWarehouseIds,
                forwardParentById,
                backwardVisitedWarehouseIds
            )
            if (forwardMeetingWarehouse != null) {
                return buildCompletePath(
                    forwardMeetingWarehouse,
                    forwardParentById,
                    backwardParentById
                )
            }
            val backwardMeetingWarehouse = performBackwardSearchStep(
                backwardQueue,
                backwardVisitedWarehouseIds,
                backwardParentById,
                forwardVisitedWarehouseIds
            )
            if (backwardMeetingWarehouse != null) {
                return buildCompletePath(
                    backwardMeetingWarehouse,
                    forwardParentById,
                    backwardParentById
                )
            }
        }
        return emptyList()
    }

    private fun initializeForwardQueue(
        startWarehouse: Warehouse
    ): ArrayDeque<Warehouse> {

        val forwardQueue = ArrayDeque<Warehouse>()

        forwardQueue.addLast(startWarehouse)

        return forwardQueue
    }

    private fun initializeBackwardQueue(
        destinationWarehouse: Warehouse
    ): ArrayDeque<Warehouse> {

        val backwardQueue = ArrayDeque<Warehouse>()

        backwardQueue.addLast(destinationWarehouse)

        return backwardQueue
    }

    private fun initializeVisitedWarehouseIds(
        warehouse: Warehouse
    ): MutableSet<String> {

        val visitedWarehouseIds = mutableSetOf<String>()
        visitedWarehouseIds.add(warehouse.id)
        return visitedWarehouseIds
    }

    private fun performForwardSearchStep(
        forwardQueue: ArrayDeque<Warehouse>,
        forwardVisitedWarehouseIds: MutableSet<String>,
        forwardParentById: MutableMap<String, Warehouse>,
        backwardVisitedWarehouseIds: Set<String>
    ): Warehouse? {
        val currentLevelWarehouseCount = forwardQueue.size
        for (currentLevelIndex in 0 until currentLevelWarehouseCount) {
            val currentWarehouse = forwardQueue.removeFirst()
            evaluatedWarehouseCount++
            for (route in currentWarehouse.outgoingRoutes) {

                val nextWarehouse = route.destinationWarehouse

                if (nextWarehouse.id in forwardVisitedWarehouseIds) continue

                forwardVisitedWarehouseIds.add(nextWarehouse.id)

                forwardParentById[nextWarehouse.id] = currentWarehouse

                if (nextWarehouse.id in backwardVisitedWarehouseIds) return nextWarehouse

                forwardQueue.addLast(nextWarehouse)
            }
        }
        return null
    }

    private fun performBackwardSearchStep(
        backwardQueue: ArrayDeque<Warehouse>,
        backwardVisitedWarehouseIds: MutableSet<String>,
        backwardParentById: MutableMap<String, Warehouse>,
        forwardVisitedWarehouseIds: Set<String>
    ): Warehouse? {

        val currentLevelWarehouseCount = backwardQueue.size

        for (currentLevelIndex in 0 until currentLevelWarehouseCount) {

            val currentWarehouse = backwardQueue.removeFirst()
            evaluatedWarehouseCount++

            for (warehouse in warehouses) {
                for (route in warehouse.outgoingRoutes) {

                    val previousWarehouse = warehouse
                    val isRouteLeadingToCurrentWarehouse =
                        route.destinationWarehouse.id == currentWarehouse.id

                    val isPreviousWarehouseUnvisited = previousWarehouse.id !in backwardVisitedWarehouseIds

                    if (
                        !isRouteLeadingToCurrentWarehouse ||
                        !isPreviousWarehouseUnvisited
                    ) continue

                    backwardVisitedWarehouseIds.add(previousWarehouse.id)
                    backwardParentById[previousWarehouse.id] = currentWarehouse
                    if (previousWarehouse.id in forwardVisitedWarehouseIds) {
                        return previousWarehouse
                    }
                    backwardQueue.addLast(previousWarehouse)
                }
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

        return forwardPath + backwardPath.drop(duplicatedMeetingWarehouseCount)
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
            val nextWarehouse = backwardParentById[currentWarehouse.id] ?: break

            currentWarehouse = nextWarehouse
            backwardPath.add(currentWarehouse)
        }
        return backwardPath
    }
}

