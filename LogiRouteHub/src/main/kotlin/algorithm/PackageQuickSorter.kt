package algorithm

import domain.model.Package
import domain.model.Warehouse


class PackageQuickSorter {
    private val firstCargoQueueIndex = 0
    private val minimumPackageCountRequiredForSorting = 2
    private val singleIndexStep = 1

    fun sortAllCargoQueuesByDescendingWeight(
        warehousesById: Map<String, Warehouse>
    ) {
        for (warehouse in warehousesById.values) {
            sortWarehouseCargoQueueByDescendingWeight(
                warehouse
            )
        }
    }

    private fun sortWarehouseCargoQueueByDescendingWeight(
        warehouse: Warehouse
    ) {
        if (!doesCargoQueueRequireSorting(warehouse)) {
            return
        }

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
        val leftSectionEndIndex = finalPivotIndex - singleIndexStep

        val rightSectionStartIndex = finalPivotIndex + singleIndexStep

        quickSortCargoQueueSection(
            warehouse = warehouse,
            sectionStartIndex = sectionStartIndex,
            sectionEndIndex = leftSectionEndIndex
        )
        quickSortCargoQueueSection(
            warehouse = warehouse,
            sectionStartIndex = rightSectionStartIndex,
            sectionEndIndex = sectionEndIndex
        )
    }

    private fun dividePackagesAroundPivot(
        warehouse: Warehouse,
        sectionStartIndex: Int,
        sectionEndIndex: Int
    ): Int {
        val cargoQueue = warehouse.cargoQueue

        val pivotWeight =
            cargoQueue[sectionStartIndex].extractPackageWeight()

        var pivotIndex = sectionStartIndex

        val firstComparisonIndex = sectionStartIndex + singleIndexStep

        for (comparedPackageIndex in firstComparisonIndex..sectionEndIndex) {
            val comparedPackageWeight = cargoQueue[comparedPackageIndex].extractPackageWeight()

            val isComparedPackageHeavierThanPivot = comparedPackageWeight > pivotWeight

            if (isComparedPackageHeavierThanPivot) {
                movePackageBeforePivot(
                    warehouse = warehouse,
                    sourcePackageIndex = comparedPackageIndex,
                    pivotIndex = pivotIndex
                )
                pivotIndex += singleIndexStep
            }
        }
        return pivotIndex
    }

    private fun doesCargoQueueRequireSorting(warehouse: Warehouse): Boolean {
        return warehouse.cargoQueue.size >= minimumPackageCountRequiredForSorting
    }

    private fun isSectionAlreadySorted(
        sectionStartIndex: Int,
        sectionEndIndex: Int
    ): Boolean {
        return sectionStartIndex >= sectionEndIndex
    }

    private fun movePackageBeforePivot(
        warehouse: Warehouse,
        sourcePackageIndex: Int,
        pivotIndex: Int
    ) {
        val cargoQueue = warehouse.cargoQueue

        val packageToMove = cargoQueue[sourcePackageIndex]

        val firstIndexToShift = pivotIndex + singleIndexStep

        for (indexToShift in sourcePackageIndex downTo firstIndexToShift) {
            val previousPackageIndex = indexToShift - singleIndexStep
            val previousPackage = cargoQueue[previousPackageIndex]
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

    private fun Package.extractPackageWeight(): Double {
        return this.weightByKg
    }
}
