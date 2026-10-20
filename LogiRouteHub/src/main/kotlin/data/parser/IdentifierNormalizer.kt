package data.parser

internal object IdentifierNormalizer {

    private const val REQUIRED_ID_PARTS = 2
    private const val REQUIRED_ID_DIGITS = 6

    private const val PREFIX_INDEX = 0
    private const val NUMERIC_PART_INDEX = 1

    fun normalize(identifier: String): String {
        val normalized = identifier.trim().uppercase()
        val parts = normalized.split("-")

        if (parts.size != REQUIRED_ID_PARTS) {
            return normalized
        }

        val prefix = parts[PREFIX_INDEX]
        val numericPart = parts[NUMERIC_PART_INDEX]

        if (prefix.isBlank() || numericPart.toIntOrNull() == null) {
            return normalized
        }

        return "$prefix-${numericPart.padStart(REQUIRED_ID_DIGITS, '0')}"
    }
}
