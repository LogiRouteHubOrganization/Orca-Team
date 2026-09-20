package domain.usecase

import domain.model.Package
import domain.model.Warehouse
import domain.model.request.PackagePriorityLevels
import domain.repository.WarehouseRepository

class PrioritizeDispatchQueueUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val priorityLevels: PackagePriorityLevels
) {

    suspend operator fun invoke(
        warehouseId: String
    ): Result<Warehouse?> = runCatching {

        val warehouse = warehouseRepository.findById(warehouseId).getOrThrow()
            ?: return@runCatching null

        val prioritizedPackages = warehouse.cargoQueue
            .sortedWith(compareByDescending<Package> {
                    getPriorityLevel(it.priority)
                }.thenByDescending {
                    it.weightByKg
                }
            )

        prioritizedPackages.forEachIndexed { index, logisticsPackage ->
            warehouse.updatePackageAtIndex(
                packageIndex = index,
                logisticsPackage = logisticsPackage
            )
        }
        warehouse
    }
    private fun getPriorityLevel(priority: String): Int {
        return when (priority.uppercase()) {
            "URGENT" -> priorityLevels.urgent
            "STANDARD" -> priorityLevels.standard
            "LOW" -> priorityLevels.low
            else -> priorityLevels.unknown
        }
    }
}
