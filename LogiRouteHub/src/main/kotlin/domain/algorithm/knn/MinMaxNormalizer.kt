
package domain.algorithm.knn

class MinMaxNormalizer {

    fun normalize(
        featureValue: Double,
        minimumValue: Double,
        maximumValue: Double
    ): Double {
        require(featureValue.isFinite()) {
            "Feature value must be finite"
        }

        require(minimumValue.isFinite() && maximumValue.isFinite()) {
            "Normalization bounds must be finite"
        }

        require(minimumValue <= maximumValue) {
            "Minimum value cannot exceed maximum value"
        }

        if (minimumValue == maximumValue) {
            return 0.0
        }

        val valueOffset = featureValue - minimumValue
        val featureRange = maximumValue - minimumValue

        val normalizedValue = valueOffset / featureRange

        return normalizedValue
    }
}
  