package domain.usecase

import domain.model.Warehouse
import domain.routing.LeastHopRouter

class FindFewestHopsRouteUseCase (private val router: LeastHopRouter){
    operator fun invoke(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse): List<Warehouse> {
        return router.findLeastHopPath(
            startWarehouse,
            destinationWarehouse
        )
    }
}


