package algorithm

import dataholder.LogisticsPackageRaw

private const val urgentPriority = "URGENT"
private const val standardPriority= "STANDARD"
private const val lowPriority = "LOW"

private const val urgentPriorityLevel = 3
private const val standardPriorityLevel = 2
private const val lowPriorityLevel = 1

private const val firstPackageIndex = 0
private const val singleIndexStep = 1

fun sortPackages(logisticsPackages: MutableList<LogisticsPackageRaw>) {
    sortPackagesByWeight(logisticsPackages)
    sortPackagesByPriority(logisticsPackages)
}

private fun sortPackagesByWeight(logisticsPackages: MutableList<LogisticsPackageRaw>) {
    for (currentSortingIndex in firstPackageIndex  until logisticsPackages.lastIndex) {
        val packageWithHighestWeightIndex = findPackageWithHighestWeightIndex(
            logisticsPackages = logisticsPackages,
            searchStartingIndex = currentSortingIndex
        )
        placePackageAtSortingIndex(
            logisticsPackages = logisticsPackages,
            packageCurrentIndex = packageWithHighestWeightIndex,
            requiredSortingIndex = currentSortingIndex
        )
    }
}

private fun findPackageWithHighestWeightIndex(
    logisticsPackages: MutableList<LogisticsPackageRaw>,
    searchStartingIndex: Int
): Int {
    var packageWithHighestWeightIndex =
        searchStartingIndex

    val firstPackageIndexToCompare = searchStartingIndex + singleIndexStep

    for (currentPackageIndex in firstPackageIndexToCompare until logisticsPackages.size) {

        val currentPackage = logisticsPackages[currentPackageIndex]

        val packageWithHighestWeight = logisticsPackages[packageWithHighestWeightIndex]

        if (hasHigherWeight(currentPackage, packageWithHighestWeight)) {
            packageWithHighestWeightIndex =
                currentPackageIndex
        }
    }

    return packageWithHighestWeightIndex
}

private fun hasHigherWeight(
    currentPackage: LogisticsPackageRaw,
    packageWithHighestWeight: LogisticsPackageRaw
): Boolean {

    return currentPackage.packageWeightByKg > packageWithHighestWeight.packageWeightByKg
}

private fun sortPackagesByPriority(
    logisticsPackages: MutableList<LogisticsPackageRaw>
) {
    for (
    currentSortingIndex in
    firstPackageIndex  until logisticsPackages.lastIndex
    ) {
        val packageWithHighestPriorityIndex =
            findPackageWithHighestPriorityIndex(
                logisticsPackages = logisticsPackages,
                searchStartingIndex = currentSortingIndex
            )
        placePackageAtSortingIndex(
            logisticsPackages = logisticsPackages,
            packageCurrentIndex = packageWithHighestPriorityIndex,
            requiredSortingIndex = currentSortingIndex
        )
    }
}

private fun findPackageWithHighestPriorityIndex(
    logisticsPackages: MutableList<LogisticsPackageRaw>,
    searchStartingIndex: Int
): Int {
    var packageWithHighestPriorityIndex = searchStartingIndex

    val firstPackageIndexToCompare = searchStartingIndex + singleIndexStep

    for (currentPackageIndex in firstPackageIndexToCompare until logisticsPackages.size) {

        val currentPackage = logisticsPackages[currentPackageIndex]

        val packageWithHighestPriority = logisticsPackages[packageWithHighestPriorityIndex]

        if (hasHigherPriority(currentPackage, packageWithHighestPriority)) {
            packageWithHighestPriorityIndex =
                currentPackageIndex
        }
    }
    return packageWithHighestPriorityIndex
}

private fun hasHigherPriority(
    currentPackage: LogisticsPackageRaw,
    packageWithHighestPriority: LogisticsPackageRaw
): Boolean {

    val currentPackagePriorityLevel = getPriorityLevel(
        currentPackage.packagePriority
    )
    val highestPackagePriorityLevel = getPriorityLevel(
        packageWithHighestPriority.packagePriority
    )

    return currentPackagePriorityLevel > highestPackagePriorityLevel
}

private fun getPriorityLevel(
    priority: String
): Int {
    return when (priority.uppercase()) {
        urgentPriority -> urgentPriorityLevel

        standardPriority-> standardPriorityLevel

        lowPriority -> lowPriorityLevel

        else -> lowPriorityLevel
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
    val packageToPlace = logisticsPackages.removeAt(
        packageCurrentIndex
    )
    logisticsPackages.add(
        requiredSortingIndex,
        packageToPlace
    )
}
