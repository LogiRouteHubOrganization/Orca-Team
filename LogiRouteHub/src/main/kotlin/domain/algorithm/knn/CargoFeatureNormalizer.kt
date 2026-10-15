
package domain.algorithm.knn

class CargoFeatureNormalizer(
    trainingRecords: List<CargoTrainingRecord>,
    private val minMaxNormalizer: MinMaxNormalizer = MinMaxNormalizer()
) {
    private val minimumLatitude: Double
    private val maximumLatitude: Double

    private val minimumLongitude: Double
    private val maximumLongitude: Double

    private val minimumWeight: Double
    private val maximumWeight: Double

    init {
        require(trainingRecords.isNotEmpty()) {
            "Training records cannot be empty"
        }

        val trainingFeatures = trainingRecords.map { it.features }

        require(trainingFeatures.all { features ->
            features.latitude.isFinite() && features.longitude.isFinite() && features.weightKg.isFinite()
        }) {
            "Training features must contain finite values"
        }

        minimumLatitude = trainingFeatures.minOf { it.latitude }
        maximumLatitude = trainingFeatures.maxOf { it.latitude }

        minimumLongitude = trainingFeatures.minOf { it.longitude }
        maximumLongitude = trainingFeatures.maxOf { it.longitude }

        minimumWeight = trainingFeatures.minOf { it.weightKg }
        maximumWeight = trainingFeatures.maxOf { it.weightKg }
    }

    fun normalize(features: CargoFeatures): CargoFeatures {
        return CargoFeatures(
            latitude = minMaxNormalizer.normalize(
                featureValue = features.latitude,
                minimumValue = minimumLatitude,
                maximumValue = maximumLatitude
            ),
            longitude = minMaxNormalizer.normalize(
                featureValue = features.longitude,
                minimumValue = minimumLongitude,
                maximumValue = maximumLongitude
            ),
            weightKg = minMaxNormalizer.normalize(
                featureValue = features.weightKg,
                minimumValue = minimumWeight,
                maximumValue = maximumWeight
            )
        )
    }
}
