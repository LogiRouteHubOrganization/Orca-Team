package domain.usecase

import domain.model.Warehouse
import domain.routing.LeastHopRouter

class CheckWarehouseConnectivityUseCase(
    private val leastHopRouter: LeastHopRouter
) {
    operator fun invoke(
        startWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Boolean {
        val warehousePath = leastHopRouter.findLeastHopPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        return warehousePath.isNotEmpty()
    }
}
