package domain.usecase

import domain.algorithm.sorting.PackageQuickSorter
import domain.model.Package
import domain.repository.WarehouseRepository

class AssignPackageToCargoQueueUseCase(
    private val packageQuickSorter: PackageQuickSorter,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(logisticsPackage: Package) {

        val originWarehouse = logisticsPackage.originWarehouse

        warehouseRepository.addPackage(
            warehouse = originWarehouse,
            logisticsPackage = logisticsPackage
        )

        packageQuickSorter.sortCargoQueue(originWarehouse)

        warehouseRepository.update(originWarehouse)
    }
}
