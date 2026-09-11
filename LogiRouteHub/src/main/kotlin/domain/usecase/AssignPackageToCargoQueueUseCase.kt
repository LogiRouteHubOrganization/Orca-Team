package domain.usecase

import domain.model.Package
import domain.repository.WarehouseRepository

class AssignPackageToCargoQueueUseCase(
    private val sortCargoQueue: SortingCargoQueueUseCase,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(logisticsPackage: Package) {
        val originWarehouse = logisticsPackage.originWarehouse

        originWarehouse.addPackage(logisticsPackage)

        sortCargoQueue(originWarehouse)

        warehouseRepository.update(originWarehouse)
    }
}
