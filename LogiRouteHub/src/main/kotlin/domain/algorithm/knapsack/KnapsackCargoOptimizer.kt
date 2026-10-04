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

        val selectedPackages = mutableListOf<Package>()
        var currentWeight = 0.0

        for (logisticsPackage in packages) {
            if (currentWeight + logisticsPackage.weightByKg <= capacityKg) {
                selectedPackages.add(logisticsPackage)
                currentWeight += logisticsPackage.weightByKg
            }
        }

        return selectedPackages
    }
}
