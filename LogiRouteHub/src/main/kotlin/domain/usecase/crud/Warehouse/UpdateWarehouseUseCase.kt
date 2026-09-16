package domain.usecase.crud.Warehouse

import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class UpdateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        warehouseId: String,
        updateModel: WarehouseUpdateModel
    ): Warehouse {
        return warehouseRepository.update(
            warehouseId,
            updateModel
        )
    }
}