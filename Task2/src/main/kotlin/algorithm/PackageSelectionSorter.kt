package algorithm

import model.LogisticsPackageRow

class PackageSelectionSorter {

    fun sortPackagesByPriorityAndWeight(logisticsPackageRows: MutableList<LogisticsPackageRow>) {

        for (currentIndex in 0 until logisticsPackageRows.size - 1) {

            val bestPackageIndex = findBestPackageIndex(logisticsPackageRows, currentIndex)

            movePackageToIndex(logisticsPackageRows, bestPackageIndex, currentIndex)
        }
    }

    private fun findBestPackageIndex(logisticsPackageRows: List<LogisticsPackageRow>, startIndex: Int): Int {

        var bestPackageIndex = startIndex

        for (comparisonIndex in startIndex + 1 until logisticsPackageRows.size) {

            val comparisonPackage = logisticsPackageRows[comparisonIndex]

            val bestPackage = logisticsPackageRows[bestPackageIndex]

            if (shouldComeBefore(comparisonPackage, bestPackage)) {
                bestPackageIndex = comparisonIndex
            }
        }

        return bestPackageIndex
    }

    private fun shouldComeBefore(
        comparisonPackage: LogisticsPackageRow,
        bestPackage: LogisticsPackageRow
    ): Boolean {

        val comparisonPriorityValue =
            getPriorityValue(comparisonPackage.priority)

        val bestPriorityValue =
            getPriorityValue(bestPackage.priority)

        if (comparisonPriorityValue > bestPriorityValue) {
            return true
        }

        return comparisonPriorityValue == bestPriorityValue &&
                comparisonPackage.packageWeight > bestPackage.packageWeight
    }

    private fun getPriorityValue(priority: String): Int {

        return when (priority) {
            "URGENT" -> 3
            "STANDARD" -> 2
            else -> 1
        }
    }

    private fun movePackageToIndex(logisticsPackageRows: MutableList<LogisticsPackageRow>, packageIndex: Int, targetIndex: Int) {
        if (packageIndex == targetIndex) {
            return
        }
        // Moving instead of swapping keeps equal packages in their original order.
        val selectedPackage = logisticsPackageRows.removeAt(packageIndex)

        logisticsPackageRows.add(targetIndex, selectedPackage)
    }
}