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
        return emptyList()
    }
}