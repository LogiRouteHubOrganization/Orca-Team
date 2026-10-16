
package domain.algorithm.knn

import kotlin.math.sqrt

class EuclideanDistance {

    fun calculate(
        firstCargo: CargoFeatures,
        secondCargo: CargoFeatures
    ): Double {
        require(
            firstCargo.latitude.isFinite() && firstCargo.longitude.isFinite() &&
                    firstCargo.weightKg.isFinite() && secondCargo.latitude.isFinite() &&
                    secondCargo.longitude.isFinite() && secondCargo.weightKg.isFinite()
        ) {
            "Cargo features must contain finite values"
        }

        val latitudeDifference = firstCargo.latitude - secondCargo.latitude

        val longitudeDifference = firstCargo.longitude - secondCargo.longitude

        val weightDifference = firstCargo.weightKg - secondCargo.weightKg

        val squaredDistance = latitudeDifference * latitudeDifference +
                longitudeDifference * longitudeDifference +
                    weightDifference * weightDifference

        return sqrt(squaredDistance)
    }
}
