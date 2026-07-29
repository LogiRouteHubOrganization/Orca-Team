package algorithm

import dataHolder.LogisticsPackageRaw

private const val URGENT_PRIORITY = "URGENT"
private const val STANDARD_PRIORITY = "STANDARD"
private const val LOW_PRIORITY = "LOW"

private const val URGENT_PRIORITY_LEVEL = 3
private const val STANDARD_PRIORITY_LEVEL = 2
private const val LOW_PRIORITY_LEVEL = 1

fun sortPackages(logisticsPackages: MutableList<LogisticsPackageRaw>) {
    sortPackagesByWeight(logisticsPackages)
    sortPackagesByPriority(logisticsPackages)
}

private fun sortPackagesByWeight(logisticsPackages: MutableList<LogisticsPackageRaw>) {
    for (currentSortingIndex in 0 until logisticsPackages.size - 1) {
        val packageWithHighestWeightIndex = findPackageWithHighestWeightIndex(
                logisticsPackages,
                currentSortingIndex
            )

        placePackageAtSortingIndex(
            logisticsPackages,
            packageWithHighestWeightIndex,
            currentSortingIndex
        )
    }
}

private fun findPackageWithHighestWeightIndex(logisticsPackages: MutableList<LogisticsPackageRaw>, searchStartingIndex: Int): Int {
    var packageWithHighestWeightIndex = searchStartingIndex
    for (currentPackageIndex in searchStartingIndex + 1 until logisticsPackages.size) {
        val currentPackage = logisticsPackages[currentPackageIndex]

        val packageWithHighestWeight = logisticsPackages[packageWithHighestWeightIndex]

        if (hasHigherWeight(currentPackage, packageWithHighestWeight)) {
            packageWithHighestWeightIndex = currentPackageIndex
        }
    }

    return packageWithHighestWeightIndex
}

private fun hasHigherWeight(currentPackage: LogisticsPackageRaw, packageWithHighestWeight: LogisticsPackageRaw): Boolean {
    return currentPackage.packageWeightByKg >
            packageWithHighestWeight.packageWeightByKg
}

private fun sortPackagesByPriority(logisticsPackages: MutableList<LogisticsPackageRaw>) {

    for (currentSortingIndex in 0 until logisticsPackages.size - 1) {
        val packageWithHighestPriorityIndex = findPackageWithHighestPriorityIndex(
                logisticsPackages,
                currentSortingIndex
            )
        placePackageAtSortingIndex(
            logisticsPackages,
            packageWithHighestPriorityIndex,
            currentSortingIndex
        )
    }
}

private fun findPackageWithHighestPriorityIndex(logisticsPackages: MutableList<LogisticsPackageRaw>, searchStartingIndex: Int): Int {
    var packageWithHighestPriorityIndex = searchStartingIndex
    for (currentPackageIndex in searchStartingIndex + 1 until logisticsPackages.size) {
        val currentPackage =
            logisticsPackages[currentPackageIndex]

        val packageWithHighestPriority =
            logisticsPackages[packageWithHighestPriorityIndex]

        if (
            hasHigherPriority(
                currentPackage,
                packageWithHighestPriority
            )
        ) {
            packageWithHighestPriorityIndex =
                currentPackageIndex
        }
    }

    return packageWithHighestPriorityIndex
}

private fun hasHigherPriority(currentPackage: LogisticsPackageRaw, packageWithHighestPriority: LogisticsPackageRaw): Boolean {
    val currentPackagePriorityLevel = getPriorityLevel(currentPackage.packagePriority)

    val highestPackagePriorityLevel = getPriorityLevel(packageWithHighestPriority.packagePriority)

    return currentPackagePriorityLevel > highestPackagePriorityLevel
}

private fun getPriorityLevel(priority: String): Int {
    return when (priority.uppercase()) {
        URGENT_PRIORITY -> URGENT_PRIORITY_LEVEL

        STANDARD_PRIORITY -> STANDARD_PRIORITY_LEVEL

        LOW_PRIORITY -> LOW_PRIORITY_LEVEL

        else -> LOW_PRIORITY_LEVEL
    }
}

private fun placePackageAtSortingIndex(
    logisticsPackages: MutableList<LogisticsPackageRaw>,
    packageCurrentIndex: Int,
    requiredSortingIndex: Int
) {
    if (packageCurrentIndex == requiredSortingIndex) {
        return
    }

    val packageToPlace = logisticsPackages.removeAt(packageCurrentIndex)

    logisticsPackages.add(requiredSortingIndex, packageToPlace)
}