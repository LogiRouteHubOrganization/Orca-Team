package domain.usecase.crud.Warehouse

import domain.repository.WarehouseRepository

class DeleteWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(warehouseId: String) {
        warehouseRepository.delete(warehouseId)
    }
}