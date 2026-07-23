package parser

import dataHolder.LogisticsPackageRaw
import java.io.File

private const val CSV_SEPARATOR = ","

private const val HEADER_ROW_COUNT = 1
private const val EXPECTED_COLUMN_COUNT = 4

private const val PACKAGE_ID_COLUMN_INDEX = 0
private const val PACKAGE_WEIGHT_COLUMN_INDEX = 1
private const val DESTINATION_HUB_ID_COLUMN_INDEX = 2
private const val PRIORITY_COLUMN_INDEX = 3

private const val URGENT_PRIORITY = "URGENT"
private const val STANDARD_PRIORITY = "STANDARD"
private const val LOW_PRIORITY = "LOW"

class PackageParser {

    fun readLogisticsPackageRowsFromFile(filePath: String): List<LogisticsPackageRaw> {

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

    private fun readPackageCsvRows(filePath: String): List<String> {
        return File(filePath).readLines()
    }

    private fun parsePackageWeightByKg(packageWeight: String, packageCsvRow: String): Double? {

        val packageWeightByKg = packageWeight.trim().toDoubleOrNull()

        if (packageWeightByKg == null) {
            println("Warning: Invalid package weight -> $packageCsvRow")
            return null
        }

        return packageWeightByKg
    }

    private fun createLogisticsPackageRow(packageCsvRow: String): LogisticsPackageRaw? {

        val packageColumns = packageCsvRow.split(CSV_SEPARATOR)

        if (packageColumns.size != EXPECTED_COLUMN_COUNT) {
            println("Warning: Invalid package row -> $packageCsvRow")
            return null
        }
        val packageId = packageColumns[PACKAGE_ID_COLUMN_INDEX].trim()
        val packageWeightByKg = parsePackageWeightByKg(
            packageColumns[PACKAGE_WEIGHT_COLUMN_INDEX],
            packageCsvRow
        )

        if (packageWeightByKg == null) {
            return null
        }

        val destinationHubId = packageColumns[DESTINATION_HUB_ID_COLUMN_INDEX].trim()

        val packagePriority = standardizePriority(
            packageColumns[PRIORITY_COLUMN_INDEX]
        )

        return LogisticsPackageRaw(
            packageId = packageId,
            packageWeightByKg = packageWeightByKg,
            destinationHubId = destinationHubId,
            packagePriority = packagePriority
        )
    }

    private fun standardizePriority(priority: String): String {

        return when (priority.trim().uppercase()) {
            URGENT_PRIORITY -> URGENT_PRIORITY
            STANDARD_PRIORITY -> STANDARD_PRIORITY
            LOW_PRIORITY -> LOW_PRIORITY
            else -> LOW_PRIORITY
        }
    }
}