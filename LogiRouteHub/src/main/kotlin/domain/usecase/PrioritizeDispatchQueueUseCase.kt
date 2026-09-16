package domain.usecase

import domain.model.Package
import domain.model.Warehouse
import domain.model.request.PackagePriorityLevels
import domain.model.updateinput.WarehouseUpdateModel
import domain.repository.WarehouseRepository

class PrioritizeDispatchQueueUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val priorityLevels: PackagePriorityLevels
) {

    suspend operator fun invoke(warehouseId: String): Warehouse? {

        val warehouse = warehouseRepository.findById(warehouseId)
            ?: return null

        val prioritizedPackages = warehouse.cargoQueue
            .sortedWith(
                compareByDescending<Package> {
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

        val warehouseUpdateModel = WarehouseUpdateModel()

        warehouseRepository.update(warehouse.id, warehouseUpdateModel)

        return warehouseRepository.findById(warehouseId)
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
