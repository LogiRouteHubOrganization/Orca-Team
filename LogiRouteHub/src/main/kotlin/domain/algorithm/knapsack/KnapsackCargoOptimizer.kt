package domain.algorithm.knapsack

import domain.model.Package

class KnapsackCargoOptimizer {

    fun optimize(
        packages: List<Package>,
        capacityKg: Double
    ): List<Package> {

        if (packages.isEmpty()) {
            return emptyList()
        }
        return packages.filter { logisticsPackage ->
            logisticsPackage.weightByKg <= capacityKg
        }
    }
}
