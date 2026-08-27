package domain.usecase

import algorithm.PackageQuickSorter
import domain.model.Package
import domain.model.Warehouse

class AssignPackageToCargoQueueUseCase(
    private val packageQuickSorter: PackageQuickSorter
) {

    operator fun invoke(logisticsPackage: Package) {
        val originWarehouse = logisticsPackage.originWarehouse

        originWarehouse.addPackage(logisticsPackage)
        packageQuickSorter.sortCargoQueue(originWarehouse)
    }
}