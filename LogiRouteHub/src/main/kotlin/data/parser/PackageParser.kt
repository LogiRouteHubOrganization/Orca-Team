
package data.parser

import data.dataholder.LogisticsPackageRaw

class PackageParser {

    private companion object {
        const val CSV_SEPARATOR = ","
        const val EXPECTED_COLUMN_COUNT = 5
        const val PACKAGE_ID_INDEX = 0
        const val PACKAGE_WEIGHT_INDEX = 1
        const val ORIGIN_HUB_ID_INDEX = 2
        const val DESTINATION_HUB_ID_INDEX = 3
        const val PRIORITY_INDEX = 4

        val ALLOWED_PRIORITIES = setOf(
            "URGENT",
            "STANDARD",
            "LOW"
        )
    }

    fun parsePackageRow(
        packageCsvRow: String
    ): CsvRowParseResult<LogisticsPackageRaw> {
        val columns = packageCsvRow.split(CSV_SEPARATOR)

        if (columns.size != EXPECTED_COLUMN_COUNT) {
            return CsvRowParseResult.Failure(
                "Expected $EXPECTED_COLUMN_COUNT columns, " + "but found ${columns.size}.")
        }
        val identifiersResult = parseIdentifiers(columns)
        if (identifiersResult is CsvRowParseResult.Failure) {
            return identifiersResult
        }
        val identifiers = (identifiersResult as CsvRowParseResult.Success).value

        val weightResult = parseWeight(columns[PACKAGE_WEIGHT_INDEX])
        if (weightResult is CsvRowParseResult.Failure) {
            return weightResult
        }
        val weight = (weightResult as CsvRowParseResult.Success).value

        val priorityResult = parsePriority(columns[PRIORITY_INDEX])
        if (priorityResult is CsvRowParseResult.Failure) {
            return priorityResult
        }

        val priority = (priorityResult as CsvRowParseResult.Success).value

        return CsvRowParseResult.Success(
            LogisticsPackageRaw(
                packageId = identifiers.packageId,
                packageWeightByKg = weight,
                originHubId = identifiers.originHubId,
                destinationHubId = identifiers.destinationHubId,
                packagePriority = priority
            )
        )
    }

    private fun parseIdentifiers(
        columns: List<String>
    ): CsvRowParseResult<PackageIdentifiers> {
        val packageId = IdentifierNormalizer.normalize(
            columns[PACKAGE_ID_INDEX]
        )
        val originHubId = IdentifierNormalizer.normalize(
            columns[ORIGIN_HUB_ID_INDEX]
        )
        val destinationHubId = IdentifierNormalizer.normalize(
            columns[DESTINATION_HUB_ID_INDEX]
        )

        if (packageId.isBlank() || originHubId.isBlank() || destinationHubId.isBlank()) {
            return CsvRowParseResult.Failure(
                "Package ID, origin hub ID, and destination hub ID " +
                        "must not be blank."
            )
        }

        return CsvRowParseResult.Success(
            PackageIdentifiers(
                packageId = packageId,
                originHubId = originHubId,
                destinationHubId = destinationHubId
            )
        )
    }

    private fun parseWeight(
        weightText: String
    ): CsvRowParseResult<Double> {
        val weight = weightText.trim().toDoubleOrNull()
            ?: return CsvRowParseResult.Failure(
                "Package weight must be a valid number."
            )

        if (!weight.isFinite() || weight <= 0.0) {
            return CsvRowParseResult.Failure(
                "Package weight must be a finite positive number."
            )
        }

        return CsvRowParseResult.Success(weight)
    }

    private fun parsePriority(
        priorityText: String
    ): CsvRowParseResult<String> {
        val priority = priorityText.trim().uppercase()

        if (priority !in ALLOWED_PRIORITIES) {
            return CsvRowParseResult.Failure(
                "Invalid package priority '$priority'. " +
                        "Allowed values: ${ALLOWED_PRIORITIES.joinToString()}."
            )
        }

        return CsvRowParseResult.Success(priority)
    }

    fun toCsvRow(
        logisticsPackage: LogisticsPackageRaw
    ): String {
        return listOf(
            logisticsPackage.packageId,
            logisticsPackage.packageWeightByKg,
            logisticsPackage.originHubId,
            logisticsPackage.destinationHubId,
            logisticsPackage.packagePriority
        ).joinToString(CSV_SEPARATOR)
    }

    private data class PackageIdentifiers(
        val packageId: String,
        val originHubId: String,
        val destinationHubId: String
    )
}
