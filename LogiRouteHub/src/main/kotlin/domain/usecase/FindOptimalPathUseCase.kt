package domain.usecase

import domain.model.Warehouse
import domain.repository.WarehouseRepository
import domain.routing.ShortestPathFinder

class FindOptimalPathUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val shortestPathFinder: ShortestPathFinder
) {
    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): List<Warehouse> {
        val startWarehouse =
            warehouseRepository.findById(startWarehouseId) ?: return emptyList()

        val destinationWarehouse =
            warehouseRepository.findById(destinationWarehouseId) ?: return emptyList()

        return shortestPathFinder.findShortestPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}