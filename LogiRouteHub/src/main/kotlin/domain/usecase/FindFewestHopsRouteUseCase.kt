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
    ): Result<List<Warehouse>> = runCatching {

        val startWarehouse = warehouseRepository.findById(startWarehouseId).getOrThrow()
            ?: return@runCatching emptyList()

        val destinationWarehouse = warehouseRepository.findById(destinationWarehouseId).getOrThrow()
            ?: return@runCatching emptyList()

        pathFinder.findLeastHopPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}
