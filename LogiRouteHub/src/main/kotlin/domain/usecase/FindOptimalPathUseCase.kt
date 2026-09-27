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
    ): Result<List<Warehouse>> = runCatching {

        val startWarehouse = warehouseRepository.findById(startWarehouseId).getOrThrow()
            ?: return@runCatching emptyList()

        val destinationWarehouse = warehouseRepository.findById(destinationWarehouseId).getOrThrow()
            ?: return@runCatching emptyList()

        shortestPathFinder.findShortestPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}
