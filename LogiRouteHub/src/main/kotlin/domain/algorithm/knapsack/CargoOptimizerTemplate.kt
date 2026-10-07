package domain.algorithm.knapsack

import domain.model.Package

abstract class CargoOptimizerTemplate {

    protected companion object {
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
        return packages.isNotEmpty() && capacityKg > ZERO_CAPACITY
    }

    protected abstract fun performOptimization(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): List<Package>
}
