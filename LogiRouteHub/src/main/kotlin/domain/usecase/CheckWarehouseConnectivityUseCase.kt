package domain.usecase

import domain.extension.runCatchingCancellable
import domain.repository.WarehouseRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class CheckWarehouseConnectivityUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val findFewestHopsRouteUseCase: FindFewestHopsRouteUseCase
) {

    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): Result<Boolean> = runCatchingCancellable {

        val (startWarehouse, destinationWarehouse) = coroutineScope {
            val startDeferred = async { warehouseRepository.findById(startWarehouseId).getOrThrow() }
            val destDeferred = async { warehouseRepository.findById(destinationWarehouseId).getOrThrow() }

            Pair(startDeferred.await(), destDeferred.await())
        }

        if (startWarehouse == null || destinationWarehouse == null) {
            return@runCatchingCancellable false
        }

        val warehousePath = findFewestHopsRouteUseCase(
            startWarehouseId = startWarehouseId,
            destinationWarehouseId = destinationWarehouseId
        ).getOrThrow()

        warehousePath.isNotEmpty()
    }
}