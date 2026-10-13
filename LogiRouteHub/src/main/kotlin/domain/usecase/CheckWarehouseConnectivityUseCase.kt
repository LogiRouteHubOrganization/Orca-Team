package domain.usecase

import domain.extension.runCatchingCancellable
import domain.repository.WarehouseRepository

class CheckWarehouseConnectivityUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val findFewestHopsRouteUseCase: FindFewestHopsRouteUseCase
) {

    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): Result<Boolean> = runCatchingCancellable {

        warehouseRepository
            .findById(startWarehouseId)
            .getOrThrow()
            ?: return@runCatchingCancellable false

        warehouseRepository
            .findById(destinationWarehouseId)
            .getOrThrow()
            ?: return@runCatchingCancellable false

        val warehousePath = findFewestHopsRouteUseCase(
            startWarehouseId = startWarehouseId,
            destinationWarehouseId = destinationWarehouseId
        ).getOrThrow()

        warehousePath.isNotEmpty()
    }
}
