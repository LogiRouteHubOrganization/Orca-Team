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
    ): Boolean {
        val startWarehouse =
            warehouseRepository.findById(startWarehouseId) ?: return false

        val destinationWarehouse =
            warehouseRepository.findById(destinationWarehouseId) ?: return false

        val warehousePath = leastHopRouter.findLeastHopPath(
            startWarehouse = startWarehouse,
            destinationWarehouse = destinationWarehouse
        )

        return warehousePath.isNotEmpty()
    }
}