package domain.usecase

import domain.model.Warehouse
import domain.routing.LeastHopPathFinder

class FindFewestHopsRouteUseCase(
    private val pathFinder: LeastHopPathFinder
) {
    operator fun invoke(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): List<Warehouse> {
        return pathFinder.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )
    }
}
