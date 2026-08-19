package parser

import dataholder.LogisticsPackageRaw


class PackageParser {

    private val csvSeparator = ","
    private val expectedColumnCount = 5
    private val packageIdColumnIndex = 0
    private val packageWeightColumnIndex = 1
    private val originHubIdColumnIndex = 2
    private val destinationHubIdColumnIndex = 3
    private val priorityColumnIndex = 4
    private val urgentPriority = "URGENT"
    private val standardPriority = "STANDARD"
    private val lowPriority = "LOW"

    fun parsePackageRow(
        packageCsvRow: String
    ): LogisticsPackageRaw? {

        val packageColumns = packageCsvRow.split(csvSeparator)

        if (hasInvalidColumnCount(packageColumns)) {
            printInvalidPackageRowWarning(packageCsvRow)
            return null
        }

        val packageId = normalizeIdentifier(packageColumns[packageIdColumnIndex])
        val originHubId = normalizeIdentifier(packageColumns[originHubIdColumnIndex])
        val destinationHubId = normalizeIdentifier(packageColumns[destinationHubIdColumnIndex])

        if (hasEmptyRequiredField(packageId, originHubId, destinationHubId)) {
            printEmptyPackageFieldWarning(packageCsvRow)
            return null
        }
        val packageWeightByKg = parsePackageWeightByKg(
            packageColumns[packageWeightColumnIndex],
            packageCsvRow
        ) ?: return null
        val packagePriority = parsePackagePriority(
            packageColumns[priorityColumnIndex],
            packageCsvRow
        ) ?: return null
        return LogisticsPackageRaw(
            packageId = packageId,
            packageWeightByKg = packageWeightByKg,
            originHubId = originHubId,
            destinationHubId = destinationHubId,
            packagePriority = packagePriority
        )
    }

    private fun hasInvalidColumnCount(columns: List<String>): Boolean {
        return columns.size != expectedColumnCount
    }

    private fun hasEmptyRequiredField(
        packageId: String,
        originHubId: String,
        destinationHubId: String
    ): Boolean {
        return packageId.isEmpty() || originHubId.isEmpty() || destinationHubId.isEmpty()
    }

    private fun parsePackageWeightByKg(
        packageWeight: String,
        packageCsvRow: String
    ): Double? {
        val packageWeightByKg = packageWeight.trim().toDoubleOrNull()

        if (packageWeightByKg == null || packageWeightByKg <= 0) {
            println("Warning: Invalid package weight -> $packageCsvRow")
            return null
        }

        return packageWeightByKg
    }

    private fun parsePackagePriority(
        priority: String,
        packageCsvRow: String
    ): String? {
        return when (priority.trim().uppercase()) {
            urgentPriority -> urgentPriority
            standardPriority -> standardPriority
            lowPriority -> lowPriority
            else -> {
                println("Warning: Invalid package priority -> $packageCsvRow")
                null
            }
        }
    }

    private fun normalizeIdentifier(identifier: String): String {
        return identifier.trim().uppercase()
    }

    private fun printInvalidPackageRowWarning(packageCsvRow: String) {
        println("Warning: Invalid package row -> $packageCsvRow")
    }

    private fun printEmptyPackageFieldWarning(packageCsvRow: String) {
        println("Warning: Empty required package field -> $packageCsvRow")
    }
}
