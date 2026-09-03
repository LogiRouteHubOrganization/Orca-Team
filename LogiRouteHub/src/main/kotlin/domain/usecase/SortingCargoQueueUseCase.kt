package domain.usecase

import domain.model.Package
import domain.model.Warehouse

class SortingCargoQueueUseCase {
    private val firstCargoQueueIndex = 0
    private val minimumPackageCountRequiredForSorting = 2
    private val singleIndexStep = 1

    operator fun invoke(warehouse: Warehouse) {
        if (!doesCargoQueueRequireSorting(warehouse)) return

        quickSortCargoQueueSection(
            warehouse = warehouse,
            sectionStartIndex = firstCargoQueueIndex,
            sectionEndIndex = warehouse.cargoQueue.lastIndex
        )
    }

    private fun quickSortCargoQueueSection(
        warehouse: Warehouse,
        sectionStartIndex: Int,
        sectionEndIndex: Int
    ) {
        if (isSectionAlreadySorted(sectionStartIndex, sectionEndIndex)) return

        val finalPivotIndex = dividePackagesAroundPivot(
            warehouse = warehouse,
            sectionStartIndex = sectionStartIndex,
            sectionEndIndex = sectionEndIndex
        )

        quickSortCargoQueueSection(
            warehouse = warehouse,
            sectionStartIndex = sectionStartIndex,
            sectionEndIndex = finalPivotIndex - singleIndexStep
        )
        quickSortCargoQueueSection(
            warehouse = warehouse,
            sectionStartIndex = finalPivotIndex + singleIndexStep, sectionEndIndex = sectionEndIndex
        )
    }

    private fun dividePackagesAroundPivot(
        warehouse: Warehouse,
        sectionStartIndex: Int,
        sectionEndIndex: Int
    ): Int {
        val cargoQueue = warehouse.cargoQueue
        val pivotWeight = cargoQueue[sectionStartIndex].extractPackageWeight()
        val firstComparisonIndex = sectionStartIndex + singleIndexStep

        return (firstComparisonIndex..sectionEndIndex).
                fold(sectionStartIndex) { currentPivotIndex, comparedPackageIndex ->
            val comparedPackageWeight = cargoQueue[comparedPackageIndex].extractPackageWeight()

            if (comparedPackageWeight > pivotWeight) {
                movePackageBeforePivot(
                    warehouse = warehouse,
                    sourcePackageIndex = comparedPackageIndex,
                    pivotIndex = currentPivotIndex
                )
                currentPivotIndex + singleIndexStep
            } else {
                currentPivotIndex
            }
        }
    }

    private fun movePackageBeforePivot(
        warehouse: Warehouse,
        sourcePackageIndex: Int,
        pivotIndex: Int
    ) {
        val cargoQueue = warehouse.cargoQueue
        val packageToMove = cargoQueue[sourcePackageIndex]
        val firstIndexToShift = pivotIndex + singleIndexStep

        (sourcePackageIndex downTo firstIndexToShift).forEach { indexToShift ->
            val previousPackage = cargoQueue[indexToShift - singleIndexStep]
            warehouse.updatePackageAtIndex(
                packageIndex = indexToShift,
                logisticsPackage = previousPackage
            )
        }
        warehouse.updatePackageAtIndex(
            packageIndex = pivotIndex,
            logisticsPackage = packageToMove
        )
    }

    private fun doesCargoQueueRequireSorting(warehouse: Warehouse): Boolean {
        return warehouse.cargoQueue.size >= minimumPackageCountRequiredForSorting
    }

    private fun isSectionAlreadySorted(sectionStartIndex: Int, sectionEndIndex: Int): Boolean {
        return sectionStartIndex >= sectionEndIndex
    }

    private fun Package.extractPackageWeight(): Double = this.weightByKg
}
