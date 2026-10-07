package domain.usecase
import domain.algorithm.knapsack.CargoOptimizerTemplate
import domain.model.Package

class OptimizeCargoByWeightUseCase : CargoOptimizerTemplate() {

    operator fun invoke(
        packages: List<Package>,
        capacityKg: Double
    ): List<Package> {
        return optimize(
            packages = packages,
            capacityKg = capacityKg
        )
    }

    override fun performOptimization(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double?
    ): List<Package> {

        val capacity = scale(capacityKg)

        val weights = packages.map {
            scale(it.weightByKg)
        }

        val dp = Array(packages.size + 1) {
            IntArray(capacity + 1)
        }

        for (i in 1..packages.size) {

            val packageItem = packages[i - 1]
            val packageWeight = weights[i - 1]
            val packagePriority = getPriority(packageItem.priority)

            for (weight in 0..capacity) {

                dp[i][weight] = dp[i - 1][weight]

                if (packageWeight <= weight) {
                    dp[i][weight] = maxOf(
                        dp[i][weight],
                        dp[i - 1][weight - packageWeight] +
                                packagePriority
                    )
                }
            }
        }

        return backtrack(
            packages = packages,
            weights = weights,
            dp = dp,
            capacity = capacity
        )
    }
    private fun backtrack(
        packages: List<Package>,
        weights: List<Int>,
        dp: Array<IntArray>,
        capacity: Int
    ): List<Package> {

        val selectedPackages = mutableListOf<Package>()

        var remainingCapacity = capacity

        for (i in packages.size downTo 1) {

            if (
                dp[i][remainingCapacity] !=
                dp[i - 1][remainingCapacity]
            ) {
                selectedPackages.add(packages[i - 1])

                remainingCapacity -= weights[i - 1]
            }
        }

        return selectedPackages.reversed()
    }
}