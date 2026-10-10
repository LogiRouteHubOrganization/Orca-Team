package domain.usecase

import domain.repository.WarehouseRepository

class CheckWarehouseConnectivityUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val findFewestHopsRouteUseCase: FindFewestHopsRouteUseCase
) {

    suspend operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): Result<Boolean> = runCatching {

        warehouseRepository
            .findById(startWarehouseId)
            .getOrThrow()
            ?: return@runCatching false

        warehouseRepository
            .findById(destinationWarehouseId)
            .getOrThrow()
            ?: return@runCatching false

        val warehousePath = findFewestHopsRouteUseCase(
            startWarehouseId = startWarehouseId,
            destinationWarehouseId = destinationWarehouseId
        ).getOrThrow()

        warehousePath.isNotEmpty()
    }
}
