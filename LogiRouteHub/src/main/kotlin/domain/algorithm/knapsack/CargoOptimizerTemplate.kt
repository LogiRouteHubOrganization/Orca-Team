package domain.algorithm.knapsack

import domain.model.Package

abstract class CargoOptimizerTemplate {

    fun optimize(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double? = null
    ): List<Package> {

        if (packages.isEmpty()) {
            return emptyList()
        }

        return performOptimization(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )
    }

    protected abstract fun performOptimization(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): List<Package>
}
