package domain.usecase

import domain.model.Route
import domain.model.Warehouse
import domain.repository.WarehouseRepository

class FindOptimalPathUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): Result<List<Warehouse>> = runCatching {

        val startWarehouse = warehouseRepository
            .findById(startWarehouseId)
            .getOrThrow()
            ?: return@runCatching emptyList()

        val destinationWarehouse = warehouseRepository
            .findById(destinationWarehouseId)
            .getOrThrow()
            ?: return@runCatching emptyList()

        val warehouses = warehouseRepository
            .findAll()
            .getOrThrow()

        findShortestPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse,
            warehouses = warehouses
        )
    }

    private fun findShortestPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        warehouses: List<Warehouse>
    ): List<Warehouse> {

        if (startWarehouse.id == destinationWarehouse.id) {
            return listOf(startWarehouse)
        }

        val initialDistances = warehouses.associateBy(
            keySelector = { it },
            valueTransform = { Double.POSITIVE_INFINITY }
        ).toMutableMap()

        val initialPrevious = warehouses.associateBy(
            keySelector = { it },
            valueTransform = { null as Warehouse? }
        ).toMutableMap()

        initialDistances[startWarehouse] = 0.0

        return runDijkstra(
            currentDistances = initialDistances,
            visitedWarehouses = emptySet(),
            previousWarehouses = initialPrevious,
            destinationWarehouse = destinationWarehouse
        )
            ?.let {
                reconstructPath(
                    startWarehouse = startWarehouse,
                    destinationWarehouse = destinationWarehouse,
                    previousWarehouse = initialPrevious
                )
            }
            ?: emptyList()
    }

    private tailrec fun runDijkstra(
        currentDistances: MutableMap<Warehouse, Double>,
        visitedWarehouses: Set<Warehouse>,
        previousWarehouses: MutableMap<Warehouse, Warehouse?>,
        destinationWarehouse: Warehouse
    ): Warehouse? {

        val currentWarehouse = currentDistances
            .filterKeys { it !in visitedWarehouses }
            .minByOrNull { it.value }
            ?.key

        if (currentWarehouse == null) {
            return null
        }

        if (currentWarehouse == destinationWarehouse) {
            return currentWarehouse
        }

        val updatedDistances = currentWarehouse.outgoingRoutes
            .fold(currentDistances) { distances, route ->
                updateDistanceThroughRoute(
                    currentWarehouse = currentWarehouse,
                    route = route,
                    distanceByWarehouse = distances,
                    previousWarehouses = previousWarehouses
                )
            }

        return runDijkstra(
            currentDistances = updatedDistances,
            visitedWarehouses = visitedWarehouses + currentWarehouse,
            previousWarehouses = previousWarehouses,
            destinationWarehouse = destinationWarehouse
        )
    }

    private fun updateDistanceThroughRoute(
        currentWarehouse: Warehouse,
        route: Route,
        distanceByWarehouse: MutableMap<Warehouse, Double>,
        previousWarehouses: MutableMap<Warehouse, Warehouse?>
    ): MutableMap<Warehouse, Double> {

        val currentDistance = distanceByWarehouse[currentWarehouse]
            ?: Double.POSITIVE_INFINITY

        val newDistance = currentDistance + route.distanceKm

        val destination = route.destinationWarehouse

        val existingDistance = distanceByWarehouse[destination]
            ?: Double.POSITIVE_INFINITY

        return if (newDistance < existingDistance) {
            distanceByWarehouse[destination] = newDistance
            previousWarehouses[destination] = currentWarehouse
            distanceByWarehouse
        } else {
            distanceByWarehouse
        }
    }

    private fun reconstructPath(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse,
        previousWarehouse: Map<Warehouse, Warehouse?>
    ): List<Warehouse> {

        fun buildPath(
            currentWarehouse: Warehouse?,
            path: List<Warehouse>
        ): List<Warehouse> {

            if (currentWarehouse == null) {
                return emptyList()
            }

            val updatedPath = path + currentWarehouse

            if (currentWarehouse == startWarehouse) {
                return updatedPath.reversed()
            }

            return buildPath(
                currentWarehouse = previousWarehouse[currentWarehouse],
                path = updatedPath
            )
        }

        return buildPath(
            currentWarehouse = destinationWarehouse,
            path = emptyList()
        )
    }
}
