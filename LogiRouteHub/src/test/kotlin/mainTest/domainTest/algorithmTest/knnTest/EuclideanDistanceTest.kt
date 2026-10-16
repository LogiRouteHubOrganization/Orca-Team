
package mainTest.domainTest.algorithmTest.knnTest

import domain.algorithm.knn.CargoFeatures
import domain.algorithm.knn.EuclideanDistance
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EuclideanDistanceTest {

    private val euclideanDistance = EuclideanDistance()

    @Test
    fun `should calculate distance between two cargo features`() {
        // Given
        val firstCargo = CargoFeatures(latitude = 0.2, longitude = 0.4, weightKg = 0.6)

        val secondCargo = CargoFeatures(latitude = 0.5, longitude = 0.4, weightKg = 0.2)

        // When
        val result = euclideanDistance.calculate(firstCargo, secondCargo)

        // Then
        assertEquals(0.5, result, 0.000001)
    }

    @Test
    fun `should return zero for identical cargo features`() {
        // Given
        val cargoFeatures = CargoFeatures(latitude = 0.3, longitude = 0.5, weightKg = 0.7)

        // When
        val result = euclideanDistance.calculate(cargoFeatures, cargoFeatures)

        // Then
        assertEquals(0.0, result, 0.000001)
    }

    @Test
    fun `should calculate distance regardless of cargo order`() {
        // Given
        val firstCargo = CargoFeatures(0.1, 0.2, 0.3)
        val secondCargo = CargoFeatures(0.4, 0.6, 0.3)

        // When
        val forwardDistance = euclideanDistance.calculate(firstCargo, secondCargo)

        val reverseDistance = euclideanDistance.calculate(secondCargo, firstCargo)

        // Then
        assertEquals(forwardDistance, reverseDistance, 0.000001)
    }

    @Test
    fun `should calculate distance when only weight differs`() {
        // Given
        val firstCargo = CargoFeatures(latitude = 0.3, longitude = 0.4, weightKg = 0.2)

        val secondCargo = CargoFeatures(latitude = 0.3, longitude = 0.4, weightKg = 0.8)

        // When
        val result = euclideanDistance.calculate(firstCargo, secondCargo)

        // Then
        assertEquals(0.6, result, 0.000001)
    }

    @Test
    fun `should reject non finite cargo features`() {
        // Given
        val firstCargo = CargoFeatures(latitude = Double.NaN, longitude = 0.4, weightKg = 0.6)

        val secondCargo = CargoFeatures(latitude = 0.5, longitude = 0.4, weightKg = 0.2)

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            euclideanDistance.calculate(firstCargo, secondCargo)
        }
    }
}
