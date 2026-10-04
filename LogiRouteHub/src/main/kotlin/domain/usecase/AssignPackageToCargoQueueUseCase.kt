package domain.usecase

import domain.model.Package

class AssignPackageToCargoQueueUseCase(
    private val sortCargoQueue: SortingCargoQueueUseCase
) {

    operator fun invoke(
        logisticsPackage: Package
    ): Result<Unit> {
        return runCatching {
            val originWarehouse = logisticsPackage.originWarehouse

            originWarehouse.addPackage(logisticsPackage)

            sortCargoQueue(originWarehouse)
        }
    }
}
