package domain.algorithm.knapsack

import domain.model.Package
import domain.model.request.PackagePriorityLevels
import kotlin.math.roundToInt

class KnapsackCargoOptimizer {

    private val priorityLevels = PackagePriorityLevels()

    companion object {
        private const val SCALE_FACTOR = 100
        private const val ZERO_CAPACITY = 0.0
        private const val INITIAL_INDEX = 1
        private const val INDEX_OFFSET = 1
        private const val UNUSABLE_PACKAGE_PRIORITY = 0
        private const val DP_MATRIX_PADDING = 1
    }

    fun optimize(
        packages: List<Package>,
        capacityKg: Double
    ): List<Package> {

        if (packages.isEmpty() || capacityKg <= ZERO_CAPACITY) {
            return emptyList()
        }

        val scaledCapacity = (capacityKg * SCALE_FACTOR).roundToInt()

        val packageWeights = packages.map {
            (it.weightByKg * SCALE_FACTOR).roundToInt()
        }

        val rowCount = packages.size + DP_MATRIX_PADDING
        val columnCount = scaledCapacity + DP_MATRIX_PADDING

        val priorityDpMatrix = Array(rowCount) {
            IntArray(columnCount)
        }

        for (packageCount in INITIAL_INDEX..packages.size) {

            val packageIndex = packageCount - INDEX_OFFSET
            val currentPackage = packages[packageIndex]
            val packageWeight = packageWeights[packageIndex]
            val packagePriority = getPriority(currentPackage.priority)

            for (currentCapacity in INITIAL_INDEX..scaledCapacity) {

                val priorityWithoutCurrentPackage = priorityDpMatrix[packageCount - INDEX_OFFSET][currentCapacity]

                val isPackageFitting = packageWeight <= currentCapacity

                val priorityWithCurrentPackage = if (isPackageFitting) {
                    val remainingCapacity = currentCapacity - packageWeight
                    val previousPriority = priorityDpMatrix[packageCount - INDEX_OFFSET][remainingCapacity]
                    packagePriority + previousPriority
                } else {
                    UNUSABLE_PACKAGE_PRIORITY
                }

                priorityDpMatrix[packageCount][currentCapacity] = maxOf(
                    priorityWithoutCurrentPackage,
                    priorityWithCurrentPackage
                )
            }
        }

        val selectedPackages = mutableListOf<Package>()
        var currentCapacity = scaledCapacity

        for (packageCount in packages.size downTo INITIAL_INDEX) {

            val currentPriority = priorityDpMatrix[packageCount][currentCapacity]
            val previousPriority =
                priorityDpMatrix[packageCount - INDEX_OFFSET][currentCapacity]

            if (currentPriority != previousPriority) {
                val selectedPackage = packages[packageCount - INDEX_OFFSET]

                selectedPackages.add(selectedPackage)

                currentCapacity -=
                    (selectedPackage.weightByKg * SCALE_FACTOR).roundToInt()
            }
        }

        return selectedPackages.reversed()
    }

    private fun getPriority(priority: String): Int {
        return when (priority.uppercase()) {
            "URGENT" -> priorityLevels.urgent
            "STANDARD" -> priorityLevels.standard
            "LOW" -> priorityLevels.low
            else -> priorityLevels.unknown
        }
    }
}

