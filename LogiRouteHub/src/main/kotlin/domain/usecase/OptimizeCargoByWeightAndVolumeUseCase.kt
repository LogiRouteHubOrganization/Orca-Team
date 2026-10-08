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

        val weightCapacity = scale(capacityKg)
        val volumeCapacity = scale(capacityVolumeM3!!)

        val weights = packages.map {
            scale(it.weightByKg)
        }

        val volumes = packages.map {
            scale(it.volumeM3)
        }

        val dp = Array(packages.size + 1) {
            Array(weightCapacity + 1) {
                IntArray(volumeCapacity + 1)
            }
        }

        for (i in 1..packages.size) {

            val packageItem = packages[i - 1]
            val packageWeight = weights[i - 1]
            val packageVolume = volumes[i - 1]
            val packagePriority = getPriority(packageItem.priority)

            for (weight in 0..weightCapacity) {

                for (volume in 0..volumeCapacity) {

                    dp[i][weight][volume] =
                        dp[i - 1][weight][volume]

                    if (
                        packageWeight <= weight &&
                        packageVolume <= volume
                    ) {
                        dp[i][weight][volume] = maxOf(
                            dp[i][weight][volume],
                            dp[i - 1][
                                weight - packageWeight
                            ][
                                volume - packageVolume
                            ] + packagePriority
                        )
                    }
                }
            }
        }

        return backtrack(
            packages = packages,
            weights = weights,
            volumes = volumes,
            dp = dp,
            weightCapacity = weightCapacity,
            volumeCapacity = volumeCapacity
        )
    }

    private fun backtrack(
        packages: List<Package>,
        weights: List<Int>,
        volumes: List<Int>,
        dp: Array<Array<IntArray>>,
        weightCapacity: Int,
        volumeCapacity: Int
    ): List<Package> {

        val selectedPackages = mutableListOf<Package>()

        var remainingWeight = weightCapacity
        var remainingVolume = volumeCapacity

        for (i in packages.size downTo 1) {

            if (
                dp[i][remainingWeight][remainingVolume] !=
                dp[i - 1][remainingWeight][remainingVolume]
            ) {
                selectedPackages.add(packages[i - 1])

                remainingWeight -= weights[i - 1]
                remainingVolume -= volumes[i - 1]
            }
        }

        return selectedPackages.reversed()
    }
}