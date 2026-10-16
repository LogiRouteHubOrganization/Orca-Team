
package mainTest.domainTest.algorithmTest.knnTest

import domain.algorithm.knn.CargoFeatures
import domain.algorithm.knn.CargoTrainingRecord
import domain.algorithm.knn.KNNClassifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KNNClassifierTest {

    private val classifier = KNNClassifier()

    @Test
    fun `should classify cargo using majority vote`() {
        // Given
        val trainingRecords = listOf(
            record(0.10, "NORTH"),
            record(0.12, "NORTH"),
            record(0.14, "NORTH"),
            record(0.16, "SOUTH"),
            record(0.18, "SOUTH"),
            record(0.90, "EAST")
        )

        val featuresToClassify = features(0.13)

        // When
        val predictedSector = classifier.classify(featuresToClassify, trainingRecords)

        // Then
        assertEquals("NORTH", predictedSector)
    }

    @Test
    fun `should consider only five nearest neighbors`() {
        // Given
        val trainingRecords = listOf(
            record(0.10, "NORTH"),
            record(0.11, "NORTH"),
            record(0.12, "NORTH"),
            record(0.13, "SOUTH"),
            record(0.14, "SOUTH"),
            record(0.80, "SOUTH"),
            record(0.82, "SOUTH"),
            record(0.84, "SOUTH"),
            record(0.86, "SOUTH"),
            record(0.88, "SOUTH")
        )

        val featuresToClassify = features(0.10)

        // When
        val predictedSector = classifier.classify(featuresToClassify, trainingRecords)

        // Then
        assertEquals("NORTH", predictedSector)
    }

    @Test
    fun `should normalize features before calculating distances`() {
        // Given
        val trainingRecords = listOf(
            CargoTrainingRecord(
                CargoFeatures(0.0, 0.0, 1000.0), "NORTH"
            ),
            CargoTrainingRecord(
                CargoFeatures(0.1, 0.1, 1000.0), "NORTH"
            ),
            CargoTrainingRecord(
                CargoFeatures(0.2, 0.2, 1000.0), "NORTH"
            ),
            CargoTrainingRecord(
                CargoFeatures(1.0, 1.0, 1001.0), "SOUTH"
            ),
            CargoTrainingRecord(
                CargoFeatures(1.0, 1.0, 1001.0), "SOUTH"
            )
        )

        val featuresToClassify = CargoFeatures(0.0, 0.0, 1001.0)

        // When
        val predictedSector = classifier.classify(featuresToClassify, trainingRecords)

        // Then
        assertEquals("NORTH", predictedSector)
    }

    @Test
    fun `should reject fewer training records than k`() {
        // Given
        val trainingRecords = listOf(
            record(0.10, "NORTH"),
            record(0.20, "SOUTH"),
            record(0.30, "EAST"),
            record(0.40, "WEST")
        )

        val featuresToClassify = features(0.15)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            classifier.classify(featuresToClassify, trainingRecords)
        }
    }

    @Test
    fun `should reject empty training records`() {
        // Given
        val trainingRecords = emptyList<CargoTrainingRecord>()
        val featuresToClassify = features(0.15)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            classifier.classify(featuresToClassify, trainingRecords)
        }
    }

    @Test
    fun `should reject blank sector labels`() {
        // Given
        val trainingRecords = listOf(
            record(0.10, "NORTH"),
            record(0.20, "SOUTH"),
            record(0.30, " "),
            record(0.40, "EAST"),
            record(0.50, "WEST")
        )

        val featuresToClassify = features(0.15)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            classifier.classify(featuresToClassify, trainingRecords)
        }
    }

    @Test
    fun `should reject zero neighbors`() {
        // Given
        val invalidClassifier = KNNClassifier(k = 0)

        val trainingRecords = listOf(
            record(0.10, "NORTH"),
            record(0.20, "NORTH"),
            record(0.30, "SOUTH"),
            record(0.40, "SOUTH"),
            record(0.50, "EAST")
        )

        val featuresToClassify = features(0.15)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            invalidClassifier.classify(featuresToClassify, trainingRecords)
        }
    }

    @Test
    fun `should break vote ties using the nearest neighbor`() {
        // Given
        val trainingRecords = listOf(
            record(0.10, "NORTH"),
            record(0.20, "SOUTH"),
            record(0.30, "SOUTH"),
            record(0.40, "NORTH"),
            record(0.50, "EAST")
        )

        val featuresToClassify = features(0.10)

        // When
        val predictedSector = classifier.classify(featuresToClassify, trainingRecords)

        // Then
        assertEquals("NORTH", predictedSector)
    }

    private fun features(value: Double): CargoFeatures {
        return CargoFeatures(
            latitude = value,
            longitude = value,
            weightKg = value
        )
    }

    private fun record(
        value: Double,
        sector: String
    ): CargoTrainingRecord {
        return CargoTrainingRecord(
            features = features(value),
            sector = sector
        )
    }
}
