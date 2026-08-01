package algorithm

import domain.model.Warehouse

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
    private fun sortWarehouseCargoQueueByDescendingWeight(warehouse: Warehouse) {
        if (warehouse.cargoQueue.size < 2) {
            return
        }
        quickSortCargoQueueSection(
            warehouse = warehouse,
            startIndex = 0,
            endIndex = warehouse.cargoQueue.lastIndex
        )
    }

    private fun quickSortCargoQueueSection(
        warehouse: Warehouse,
        startIndex: Int,
        endIndex: Int
    ) {
        if (startIndex >= endIndex) {
            return
        }

        val pivotIndex = dividePackagesAroundPivot(
                warehouse = warehouse,
                startIndex = startIndex,
                endIndex = endIndex
            )

        quickSortCargoQueueSection(
            warehouse = warehouse,
            startIndex = startIndex,
            endIndex = pivotIndex - 1
        )

        quickSortCargoQueueSection(
            warehouse = warehouse,
            startIndex = pivotIndex + 1,
            endIndex = endIndex
        )
    }

    private fun dividePackagesAroundPivot(
        warehouse: Warehouse,
        startIndex: Int,
        endIndex: Int
    ): Int {

        val pivotWeight = warehouse.cargoQueue[startIndex].packageWeightByKg

        var pivotIndex = startIndex

        for (currentIndex in startIndex + 1..endIndex) {

            val currentPackageWeight = warehouse.cargoQueue[currentIndex].packageWeightByKg

            if (currentPackageWeight > pivotWeight) {

                warehouse.movePackageWithinCargoQueue(
                    currentIndex = currentIndex,
                    newIndex = pivotIndex
                )
                pivotIndex++
            }
        }

        return pivotIndex
    }
}