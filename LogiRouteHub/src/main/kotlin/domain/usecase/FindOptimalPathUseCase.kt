package domain.usecase

import domain.model.Warehouse
import domain.routing.ShortestPathRouter

class FindOptimalPathUseCase(
    private val shortestPathRouter: ShortestPathRouter
) {
    operator fun invoke(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {
        return shortestPathRouter.findShortestPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}