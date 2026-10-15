
package domain.usecase

import domain.algorithm.knn.CargoFeatures
import domain.algorithm.knn.CargoTrainingRecord
import domain.algorithm.knn.KNNClassifier
import domain.repository.PackageRepository

class ClassifyPackageSectorUseCase(
    private val packageRepository: PackageRepository,
    private val knnClassifier: KNNClassifier = KNNClassifier()
) {

    suspend operator fun invoke(
        cargoToClassify: CargoFeatures
    ): Result<String> {

        return packageRepository.findAll().mapCatching { packages ->

            val trainingRecords = packages.map { logisticsPackage ->

                val destinationWarehouse = logisticsPackage.destinationWarehouse

                val trainingFeatures = CargoFeatures(
                    latitude = destinationWarehouse.latitude,
                    longitude = destinationWarehouse.longitude,
                    weightKg = logisticsPackage.weightByKg
                )

                CargoTrainingRecord(features = trainingFeatures, sector = destinationWarehouse.regionalZone)
            }

            knnClassifier.classify(cargoToClassify = cargoToClassify, trainingRecords = trainingRecords)
        }
    }
}
