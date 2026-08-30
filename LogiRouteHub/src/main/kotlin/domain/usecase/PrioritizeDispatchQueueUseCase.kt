package domain.usecase

import domain.model.Package
import domain.model.Warehouse

private const val URGENT_PRIORITY_LEVEL = 3
private const val STANDARD_PRIORITY_LEVEL = 2
private const val LOW_PRIORITY_LEVEL = 1
private const val UNKNOWN_PRIORITY_LEVEL = 0

class PrioritizeDispatchQueueUseCase {

    operator fun invoke(warehouse: Warehouse) {

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
    }

    private fun getPriorityLevel(priority: String): Int {
        return when (priority.uppercase()) {
            "URGENT" -> URGENT_PRIORITY_LEVEL
            "STANDARD" -> STANDARD_PRIORITY_LEVEL
            "LOW" -> LOW_PRIORITY_LEVEL
            else -> UNKNOWN_PRIORITY_LEVEL
        }
    }
}