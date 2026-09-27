package domain.usecase

import domain.repository.WarehouseRepository
import domain.routing.LeastHopRouter

class CheckWarehouseConnectivityUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val leastHopRouter: LeastHopRouter
) {

    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): Result<Boolean> = runCatching {

        val startWarehouse = warehouseRepository
            .findById(startWarehouseId)
            .getOrThrow()
            ?: return@runCatching false

        val destinationWarehouse = warehouseRepository
            .findById(destinationWarehouseId)
            .getOrThrow()
            ?: return@runCatching false

        val warehousePath = leastHopRouter.findLeastHopPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )
        warehousePath.isNotEmpty()
    }
}
