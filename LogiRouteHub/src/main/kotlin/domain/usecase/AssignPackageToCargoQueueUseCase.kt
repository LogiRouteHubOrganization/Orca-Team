package domain.usecase

import domain.model.Package
import domain.repository.WarehouseRepository

class AssignPackageToCargoQueueUseCase(
    private val sortCargoQueue: SortingCargoQueueUseCase,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(logisticsPackage: Package) {
        logisticsPackage.originWarehouse.also { originWarehouse ->
            warehouseRepository.addPackage(
                warehouse = originWarehouse,
                logisticsPackage = logisticsPackage
            )

            sortCargoQueue(originWarehouse)

            warehouseRepository.update(originWarehouse)
        }
    }
}
