package parser

import model.LogisticsPackageRow
import java.io.File

class PackageParser {

    fun readLogisticsPackageRowsFromFile(filePath: String): List<LogisticsPackageRow> {

        val packageCsvRows = readPackageCsvRows(filePath)

        return packageCsvRows
            // Ignore the header because it does not represent package data.
            .drop(1)

            // Skip empty rows to avoid creating invalid package records.
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

    private fun createLogisticsPackageRow(packageCsvRow: String): LogisticsPackageRow? {

        val csvSeparator = ","
        val expectedColumnCount = 4

        val packageIdColumnIndex = 0
        val weightColumnIndex = 1
        val destinationHubIdColumnIndex = 2
        val priorityColumnIndex = 3

        val packageColumns = packageCsvRow.split(csvSeparator)

        // Skip malformed rows instead of stopping the parser.
        if (packageColumns.size != expectedColumnCount) {
            println("Warning: Invalid package row -> $packageCsvRow")
            return null
        }

        val packageId =
            packageColumns[packageIdColumnIndex].trim()

        val packageWeight =
            packageColumns[weightColumnIndex]
                .trim()
                .toDoubleOrNull()

        // Skip rows with invalid numeric values.
        if (packageWeight == null) {
            println("Warning: Invalid package weight -> $packageCsvRow")
            return null
        }

        val destinationHubId =
            packageColumns[destinationHubIdColumnIndex].trim()

        val priority =
            standardizePriority(
                packageColumns[priorityColumnIndex]
            )

        return LogisticsPackageRow(
            id = packageId,
            packageWeight = packageWeight,
            destinationHubId = destinationHubId,
            priority = priority
        )
    }

    private fun standardizePriority(priority: String): String {

        // Keep priority values consistent regardless of input formatting.
        return when (priority.trim().uppercase()) {
            "URGENT" -> "URGENT"
            "STANDARD" -> "STANDARD"
            "LOW" -> "LOW"
            else -> "LOW"
        }
    }
}