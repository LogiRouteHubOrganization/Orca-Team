package domain.usecase

import domain.algorithm.knapsack.CargoOptimizerTemplate
import domain.model.Package

class OptimizeCargoByWeightAndVolumeUseCase : CargoOptimizerTemplate() {

    operator fun invoke(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double
    ): List<Package> {
        return optimize(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        )
    }

    override fun isValidInput(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): Boolean {
        return super.isValidInput(
            packages = packages,
            capacityKg = capacityKg,
            capacityVolumeM3 = capacityVolumeM3
        ) &&
                capacityVolumeM3 != null &&
                capacityVolumeM3 > ZERO_CAPACITY
    }

    override fun performOptimization(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): List<Package> {
        return emptyList()
    }
}