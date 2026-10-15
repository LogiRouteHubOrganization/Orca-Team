
package mainTest.domainTest.algorithmTest.knnTest

import domain.algorithm.knn.CargoFeatureNormalizer
import domain.algorithm.knn.CargoFeatures
import domain.algorithm.knn.CargoTrainingRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CargoFeatureNormalizerTest {

    private val trainingRecords = listOf(
        CargoTrainingRecord(
            features = CargoFeatures(30.0, 34.0, 2.0),
            sector = "SOUTH"
        ),
        CargoTrainingRecord(
            features = CargoFeatures(31.0, 35.0, 7.0),
            sector = "CENTRAL"
        ),
        CargoTrainingRecord(
            features = CargoFeatures(32.0, 36.0, 12.0),
            sector = "NORTH"
        )
    )
    // Given
    private val normalizer = CargoFeatureNormalizer(trainingRecords)

    @Test
    fun `should normalize latitude using training bounds`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(31.5, 35.5, 9.0)
        )
        // Then
        assertEquals(0.75, result.latitude, 0.000001)
    }

    @Test
    fun `should normalize longitude using training bounds`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(31.5, 35.5, 9.0)
        )
        // Then
        assertEquals(0.75, result.longitude, 0.000001)
    }

    @Test
    fun `should normalize weight using training bounds`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(31.5, 35.5, 9.0)
        )
        // Then
        assertEquals(0.7, result.weightKg, 0.000001)
    }

    @Test
    fun `should normalize minimum latitude to zero`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(30.0, 34.0, 2.0)
        )
        // Then
        assertEquals(0.0, result.latitude, 0.000001)
    }

    @Test
    fun `should normalize minimum longitude to zero`() {

        // When
        val result = normalizer.normalize(
            CargoFeatures(30.0, 34.0, 2.0)
        )
        // Then
        assertEquals(0.0, result.longitude, 0.000001)
    }

    @Test
    fun `should normalize minimum weight to zero`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(30.0, 34.0, 2.0)
        )
        // Then
        assertEquals(0.0, result.weightKg, 0.000001)
    }

    @Test
    fun `should normalize maximum latitude to one`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(32.0, 36.0, 12.0)
        )
        // Then
        assertEquals(1.0, result.latitude, 0.000001)
    }

    @Test
    fun `should normalize maximum longitude to one`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(32.0, 36.0, 12.0)
        )
        // Then
        assertEquals(1.0, result.longitude, 0.000001)
    }

    @Test
    fun `should normalize maximum weight to one`() {
        // When
        val result = normalizer.normalize(
            CargoFeatures(32.0, 36.0, 12.0)
        )
        // Then
        assertEquals(1.0, result.weightKg, 0.000001)
    }

    @Test
    fun `should reject empty training records`() {
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            CargoFeatureNormalizer(emptyList())
        }
    }

    @Test
    fun `should reject non finite training features`() {
        // When
        val invalidRecords = listOf(
            CargoTrainingRecord(
                features = CargoFeatures(Double.NaN, 35.0, 5.0),
                sector = "NORTH"
            )
        )
       // Then
        assertThrows(IllegalArgumentException::class.java) {
            CargoFeatureNormalizer(invalidRecords)
        }
    }
}
