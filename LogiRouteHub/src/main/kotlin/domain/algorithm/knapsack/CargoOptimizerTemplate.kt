package domain.algorithm.knapsack

import domain.model.Package
import domain.model.request.PackagePriorityLevels
import kotlin.math.roundToInt

abstract class CargoOptimizerTemplate {

    private val priorityLevels = PackagePriorityLevels()

    protected companion object {
        const val SCALE_FACTOR = 100
        const val ZERO_CAPACITY = 0.0
    }

    fun optimize(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double? = null
    ): List<Package> {

        if (!isValidInput(
                packages = packages,
                capacityKg = capacityKg,
                capacityVolumeM3 = capacityVolumeM3
            )
        ) {
            return emptyList()
        }

        return performOptimization(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )
    }

    protected open fun isValidInput(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): Boolean {
        return packages.isNotEmpty() &&
                capacityKg > ZERO_CAPACITY
    }

    protected abstract fun performOptimization(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): List<Package>

    protected fun scale(value: Double): Int {
        return (value * SCALE_FACTOR).roundToInt()
    }

    protected fun getPriority(priority: String): Int {
        return when (priority.uppercase()) {
            "URGENT" -> priorityLevels.urgent
            "STANDARD" -> priorityLevels.standard
            "LOW" -> priorityLevels.low
            else -> priorityLevels.unknown
        }
    }
}