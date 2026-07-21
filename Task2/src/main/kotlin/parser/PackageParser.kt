package parser

import model.LogisticsPackageRow
import java.io.File

class PackageParser {

    fun readLogisticsPackageRowsFromFile(filePath: String): List<LogisticsPackageRow> {
        val packageCsvRows = readPackageCsvRows(filePath)

        return packageCsvRows
            .drop(1)
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

        val packageColumns = packageCsvRow.split(csvSeparator )


        if (packageColumns.size != expectedColumnCount) {
            return null
        }

        return LogisticsPackageRow(
            id = packageColumns[packageIdColumnIndex].trim(),
            weight = packageColumns[weightColumnIndex].trim(),
            destinationHubId = packageColumns[destinationHubIdColumnIndex].trim(),
            priority = packageColumns[priorityColumnIndex].trim()
        )
    }
}