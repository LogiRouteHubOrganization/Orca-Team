package domain.algorithm.sorting

import domain.model.Package

private const val URGENT_PRIORITY = "URGENT"
private const val STANDARD_PRIORITY = "STANDARD"
private const val LOW_PRIORITY = "LOW"

private const val URGENT_PRIORITY_LEVEL = 3
private const val STANDARD_PRIORITY_LEVEL = 2
private const val LOW_PRIORITY_LEVEL = 1

private const val FIRST_PACKAGE_INDEX = 0
private const val SINGLE_INDEX_STEP = 1

fun sortPackages(logisticsPackages: MutableList<Package>) {
    sortPackagesByWeight(logisticsPackages)
    sortPackagesByPriority(logisticsPackages)
}

private fun sortPackagesByWeight(logisticsPackages: MutableList<Package>) {
    for (currentSortingIndex in FIRST_PACKAGE_INDEX until logisticsPackages.lastIndex) {
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
    logisticsPackages: MutableList<Package>,
    searchStartingIndex: Int
): Int {
    var packageWithHighestWeightIndex =
        searchStartingIndex

    val firstPackageIndexToCompare = searchStartingIndex + SINGLE_INDEX_STEP

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
    currentPackage: Package,
    packageWithHighestWeight: Package
): Boolean {

    return currentPackage.weightByKg > packageWithHighestWeight.weightByKg
}

private fun sortPackagesByPriority(
    logisticsPackages: MutableList<Package>
) {
    for (
    currentSortingIndex in
    FIRST_PACKAGE_INDEX until logisticsPackages.lastIndex
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
    logisticsPackages: MutableList<Package>,
    searchStartingIndex: Int
): Int {
    var packageWithHighestPriorityIndex = searchStartingIndex

    val firstPackageIndexToCompare = searchStartingIndex + SINGLE_INDEX_STEP

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
    currentPackage: Package,
    packageWithHighestPriority: Package
): Boolean {

    val currentPackagePriorityLevel = getPriorityLevel(
        currentPackage. priority
    )
    val highestPackagePriorityLevel = getPriorityLevel(
        packageWithHighestPriority. priority
    )

    return currentPackagePriorityLevel > highestPackagePriorityLevel
}

private fun getPriorityLevel(
    priority: String
): Int {
    return when (priority.uppercase()) {
        URGENT_PRIORITY -> URGENT_PRIORITY_LEVEL

        STANDARD_PRIORITY -> STANDARD_PRIORITY_LEVEL

        LOW_PRIORITY -> LOW_PRIORITY_LEVEL

        else -> LOW_PRIORITY_LEVEL
    }
}

private fun placePackageAtSortingIndex(
    logisticsPackages: MutableList<Package>,
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
