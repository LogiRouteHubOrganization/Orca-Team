package domain.usecase

import domain.model.Warehouse
import domain.routing.ShortestPathFinder

class FindOptimalPathUseCase(
    private val shortestPathFinder: ShortestPathFinder
) {
    operator fun invoke(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {
        return shortestPathFinder.findShortestPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}
