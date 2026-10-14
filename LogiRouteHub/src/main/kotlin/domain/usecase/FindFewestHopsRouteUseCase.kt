package domain.usecase

import domain.extension.runCatchingCancellable
import domain.model.Warehouse
import domain.model.request.LeastHopRouterState
import domain.repository.WarehouseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class FindFewestHopsRouteUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): Result<List<Warehouse>> = runCatchingCancellable  {


        val (startWarehouse, destinationWarehouse) = coroutineScope {

            val startDeferred = async { warehouseRepository.findById(startWarehouseId).getOrThrow() }

            val destinationDeferred = async { warehouseRepository.findById(destinationWarehouseId).getOrThrow() }

            Pair(startDeferred.await(), destinationDeferred.await())
        }
        if (startWarehouse == null || destinationWarehouse == null) {
            return@runCatchingCancellable emptyList()
        }
        findLeastHopPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    private fun findLeastHopPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {

        if (startWarehouse.id == destinationWarehouse.id) {
            return listOf(startWarehouse)
        }

        val initialState = LeastHopRouterState(
            queue = ArrayDeque(listOf(startWarehouse)),
            visitedWarehouseIds = setOf(startWarehouse.id),
            previousWarehouseById = emptyMap()
        )

        return searchPath(
            state = initialState,
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    private fun searchPath(
        state: LeastHopRouterState,
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {

        val currentWarehouse = state.queue.firstOrNull() ?: return emptyList()

        val remainingQueue = ArrayDeque(state.queue.drop(1))
        val nextState = processOutgoingRoutes(currentWarehouse, remainingQueue, state)

        return if (destinationWarehouse.id in nextState.visitedWarehouseIds) {
            buildWarehousePath(
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse,
                previousWarehouseById = nextState.previousWarehouseById
            )
        } else {
            searchPath(
                state = nextState,
                startWarehouse = startWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }

    private fun processOutgoingRoutes(
        currentWarehouse: Warehouse,
        remainingQueue: ArrayDeque<Warehouse>,
        state: LeastHopRouterState
    ): LeastHopRouterState {
        return currentWarehouse.outgoingRoutes.fold(
            LeastHopRouterState(
                queue = remainingQueue,
                visitedWarehouseIds = state.visitedWarehouseIds,
                previousWarehouseById = state.previousWarehouseById
            )
        ) { currentState, route ->
            val nextWarehouse = route.destinationWarehouse

            if (nextWarehouse.id in currentState.visitedWarehouseIds) {
                currentState
            } else {
                currentState.copy(
                    queue = ArrayDeque(currentState.queue + nextWarehouse),
                    visitedWarehouseIds = currentState.visitedWarehouseIds + nextWarehouse.id,
                    previousWarehouseById = currentState.previousWarehouseById + (nextWarehouse.id to currentWarehouse)
                )
            }
        }
    }

    private fun buildWarehousePath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        previousWarehouseById: Map<String, Warehouse>
    ): List<Warehouse> {

        fun buildPath(
            currentWarehouse: Warehouse,
            path: List<Warehouse>
        ): List<Warehouse> {

            if (currentWarehouse.id == startWarehouse.id) {
                return (path + currentWarehouse).reversed()
            }

            val previousWarehouse =
                previousWarehouseById[currentWarehouse.id]
                    ?: return emptyList()

            return buildPath(
                currentWarehouse = previousWarehouse,
                path = path + currentWarehouse
            )
        }

        return buildPath(
            currentWarehouse = destinationWarehouse,
            path = emptyList()
        )
    }
}
