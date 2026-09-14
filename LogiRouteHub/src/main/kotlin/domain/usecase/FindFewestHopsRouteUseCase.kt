package domain.usecase

import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.routing.LeastHopPathFinder

class FindFewestHopsRouteUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val pathFinder: LeastHopPathFinder
) {
    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): List<Warehouse> {
        val startWarehouse =
            warehouseRepository.findById(startWarehouseId) ?: return emptyList()

        val destinationWarehouse =
            warehouseRepository.findById(destinationWarehouseId) ?: return emptyList()

        return pathFinder.findLeastHopPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}