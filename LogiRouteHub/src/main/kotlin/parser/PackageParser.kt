package parser

import dataHolder.LogisticsPackageRaw
import java.io.File

private const val CSV_SEPARATOR = ","
private const val HEADER_ROW_COUNT = 1
private const val EXPECTED_COLUMN_COUNT = 5

private const val PACKAGE_ID_COLUMN_INDEX = 0
private const val PACKAGE_WEIGHT_COLUMN_INDEX = 1
private const val ORIGIN_HUB_ID_COLUMN_INDEX = 2
private const val DESTINATION_HUB_ID_COLUMN_INDEX = 3
private const val PRIORITY_COLUMN_INDEX = 4

private const val URGENT_PRIORITY = "URGENT"
private const val STANDARD_PRIORITY = "STANDARD"
private const val LOW_PRIORITY = "LOW"

class PackageParser {

    fun readLogisticsPackageRowsFromFile(
        filePath: String
    ): List<LogisticsPackageRaw> {

        val packageCsvRows = readPackageCsvRows(filePath)

        return packageCsvRows
            .drop(HEADER_ROW_COUNT)
            .filter { packageCsvRow ->
                packageCsvRow.isNotBlank()
            }
            .mapNotNull { packageCsvRow ->
                createLogisticsPackageRow(packageCsvRow)
            }
    }

    private fun readPackageCsvRows(
        filePath: String
    ): List<String> {

        return File(filePath).readLines()
    }

    private fun createLogisticsPackageRow(
        packageCsvRow: String
    ): LogisticsPackageRaw? {

        val packageColumns =
            packageCsvRow.split(CSV_SEPARATOR)

        if (hasInvalidColumnCount(packageColumns)) {
            printInvalidPackageRowWarning(packageCsvRow)
            return null
        }

        val packageId =
            normalizeIdentifier(
                packageColumns[PACKAGE_ID_COLUMN_INDEX]
            )

        val originHubId =
            normalizeIdentifier(
                packageColumns[ORIGIN_HUB_ID_COLUMN_INDEX]
            )

        val destinationHubId =
            normalizeIdentifier(
                packageColumns[DESTINATION_HUB_ID_COLUMN_INDEX]
            )

        if (
            hasEmptyRequiredField(
                packageId,
                originHubId,
                destinationHubId
            )
        ) {
            printEmptyPackageFieldWarning(packageCsvRow)
            return null
        }

        val packageWeightByKg =
            parsePackageWeightByKg(
                packageColumns[PACKAGE_WEIGHT_COLUMN_INDEX],
                packageCsvRow
            )

        if (packageWeightByKg == null) {
            return null
        }

        val packagePriority =
            parsePackagePriority(
                packageColumns[PRIORITY_COLUMN_INDEX],
                packageCsvRow
            )

        if (packagePriority == null) {
            return null
        }

        return LogisticsPackageRaw(
            packageId = packageId,
            packageWeightByKg = packageWeightByKg,
            originHubId = originHubId,
            destinationHubId = destinationHubId,
            packagePriority = packagePriority
        )
    }

    private fun hasInvalidColumnCount(
        columns: List<String>
    ): Boolean {

        return columns.size != EXPECTED_COLUMN_COUNT
    }

    private fun hasEmptyRequiredField(
        packageId: String,
        originHubId: String,
        destinationHubId: String
    ): Boolean {

        return packageId.isEmpty() ||
                originHubId.isEmpty() ||
                destinationHubId.isEmpty()
    }

    private fun parsePackageWeightByKg(
        packageWeight: String,
        packageCsvRow: String
    ): Double? {

        val packageWeightByKg =
            packageWeight.trim().toDoubleOrNull()

        if (
            packageWeightByKg == null ||
            packageWeightByKg <= 0
        ) {
            println(
                "Warning: Invalid package weight -> $packageCsvRow"
            )

            return null
        }

        return packageWeightByKg
    }

    private fun parsePackagePriority(
        priority: String,
        packageCsvRow: String
    ): String? {

        return when (priority.trim().uppercase()) {
            URGENT_PRIORITY -> URGENT_PRIORITY
            STANDARD_PRIORITY -> STANDARD_PRIORITY
            LOW_PRIORITY -> LOW_PRIORITY

            else -> {
                println(
                    "Warning: Invalid package priority -> $packageCsvRow"
                )

                null
            }
        }
    }

    private fun normalizeIdentifier(
        identifier: String
    ): String {

        return identifier.trim().uppercase()
    }

    private fun printInvalidPackageRowWarning(
        packageCsvRow: String
    ) {
        println(
            "Warning: Invalid package row -> $packageCsvRow"
        )
    }

    private fun printEmptyPackageFieldWarning(
        packageCsvRow: String
    ) {
        println(
            "Warning: Empty required package field -> $packageCsvRow"
        )
    }
}