
package mainTest.domainTest.algorithmTest.knnTest

import domain.algorithm.knn.MinMaxNormalizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MinMaxNormalizerTest {
    // Given
    private val normalizer = MinMaxNormalizer()

    @Test
    fun `should normalize feature value within valid range`() {
        //When
        val normalizedValue = normalizer.normalize(
            featureValue = 7.0,
            minimumValue = 2.0,
            maximumValue = 12.0
        )
        // Then
        assertEquals(0.5, normalizedValue, 0.000001)
    }

    @Test
    fun `should return zero for minimum feature value`() {
        // When
        val normalizedValue = normalizer.normalize(
            featureValue = 2.0,
            minimumValue = 2.0,
            maximumValue = 12.0
        )
        // Then
        assertEquals(0.0, normalizedValue, 0.000001)
    }

    @Test
    fun `should return one for maximum feature value`() {
        // When
        val normalizedValue = normalizer.normalize(
            featureValue = 12.0,
            minimumValue = 2.0,
            maximumValue = 12.0
        )
      //  Then
        assertEquals(1.0, normalizedValue, 0.000001)
    }

    @Test
    fun `should return zero when minimum and maximum are equal`() {
        // When
        val normalizedValue = normalizer.normalize(
            featureValue = 5.0,
            minimumValue = 5.0,
            maximumValue = 5.0
        )
        // Then
        assertEquals(0.0, normalizedValue, 0.000001)
    }

    @Test
    fun `should reject minimum greater than maximum`() {
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            normalizer.normalize(
                featureValue = 7.0,
                minimumValue = 12.0,
                maximumValue = 2.0
            )
        }
    }

    @Test
    fun `should reject non finite feature value`() {
        // When & Then

        assertThrows(IllegalArgumentException::class.java) {
            normalizer.normalize(
                featureValue = Double.NaN,
                minimumValue = 2.0,
                maximumValue = 12.0
            )
        }
    }

    @Test
    fun `should reject non finite normalization bounds`() {
        // When & Then

        assertThrows(IllegalArgumentException::class.java) {
            normalizer.normalize(
                featureValue = 7.0,
                minimumValue = Double.POSITIVE_INFINITY,
                maximumValue = 12.0
            )
        }
    }
}
