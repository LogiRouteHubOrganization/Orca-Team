
package domain.algorithm.knn

class KNNClassifier(
    private val euclideanDistance: EuclideanDistance = EuclideanDistance(),
    private val k: Int = DEFAULT_NEIGHBOR_COUNT
) {
    companion object {
        private const val DEFAULT_NEIGHBOR_COUNT = 5
        private const val INITIAL_VOTE_COUNT = 0
        private const val VOTE_INCREMENT = 1
    }

    fun classify(
        cargoToClassify: CargoFeatures,
        trainingRecords: List<CargoTrainingRecord>
    ): String {

        require(k > 0) { "K must be greater than zero" }

        require(trainingRecords.size >= k) { "Training records must contain at least $k records" }

        require(trainingRecords.all { it.sector.isNotBlank() }) {
            "Training records must have valid sector labels"
        }

        val featureNormalizer = CargoFeatureNormalizer(trainingRecords)

        val normalizedCargoToClassify = featureNormalizer.normalize(cargoToClassify)

        val cargoDistances = trainingRecords.map { trainingRecord ->
            val normalizedTrainingCargo = featureNormalizer.normalize(trainingRecord.features)

            val distance = euclideanDistance.calculate(normalizedCargoToClassify, normalizedTrainingCargo)

            Pair(trainingRecord.sector, distance)
        }

        val sortedDistances = cargoDistances.sortedBy { it.second }
        val nearestNeighbors = sortedDistances.take(k)

        val sectorVotes = mutableMapOf<String, Int>()

        for (neighbor in nearestNeighbors) {
            val sector = neighbor.first
            sectorVotes[sector] = sectorVotes.getOrDefault(sector, INITIAL_VOTE_COUNT) + VOTE_INCREMENT
        }

        val predictedSector = sectorVotes.maxByOrNull { it.value }?.key
            ?: error("Unable to determine the predicted sector")

        return predictedSector

    }
}
