package algorithm

import domain.model.Package
import domain.model.Warehouse

private const val FIRST_CARGO_QUEUE_INDEX = 0
private const val MINIMUM_PACKAGE_COUNT_REQUIRED_FOR_SORTING = 2
private const val SINGLE_INDEX_STEP = 1

class PackageQuickSorter {

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
            sectionStartIndex = FIRST_CARGO_QUEUE_INDEX,
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
        val leftSectionEndIndex = finalPivotIndex - SINGLE_INDEX_STEP

        val rightSectionStartIndex = finalPivotIndex + SINGLE_INDEX_STEP

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

        val firstComparisonIndex = sectionStartIndex + SINGLE_INDEX_STEP

        for (comparedPackageIndex in firstComparisonIndex..sectionEndIndex) {
            val comparedPackageWeight = cargoQueue[comparedPackageIndex].extractPackageWeight()

            val isComparedPackageHeavierThanPivot = comparedPackageWeight > pivotWeight

            if (isComparedPackageHeavierThanPivot) {
                movePackageBeforePivot(
                    warehouse = warehouse,
                    sourcePackageIndex = comparedPackageIndex,
                    pivotIndex = pivotIndex
                )
                pivotIndex += SINGLE_INDEX_STEP
            }
        }
        return pivotIndex
    }

    private fun doesCargoQueueRequireSorting(warehouse: Warehouse): Boolean {
        return warehouse.cargoQueue.size >= MINIMUM_PACKAGE_COUNT_REQUIRED_FOR_SORTING
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

        val firstIndexToShift = pivotIndex + SINGLE_INDEX_STEP

        for (indexToShift in sourcePackageIndex downTo firstIndexToShift) {
            val previousPackageIndex = indexToShift - SINGLE_INDEX_STEP
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
