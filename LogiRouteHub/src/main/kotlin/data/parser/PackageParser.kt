package data.parser

import data.dataholder.LogisticsPackageRaw

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
    private val requiredIdDigits = 6

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

        return parsePackageDetails(packageColumns, packageCsvRow, packageId, originHubId, destinationHubId)
    }

    private fun parsePackageDetails(
        packageColumns: List<String>,
        packageCsvRow: String,
        packageId: String,
        originHubId: String,
        destinationHubId: String
    ): LogisticsPackageRaw? {
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

    private fun hasInvalidColumnCount(
        columns: List<String>
    ): Boolean {
        return columns.size != expectedColumnCount
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
        val packageWeightByKg = packageWeight
            .trim()
            .toDoubleOrNull()

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
        val normalizedPriority = priority
            .trim()
            .uppercase()

        return when (normalizedPriority) {
            urgentPriority -> urgentPriority
            standardPriority -> standardPriority
            lowPriority -> lowPriority
            else -> {
                println("Warning: Invalid package priority -> $packageCsvRow")
                null
            }
        }
    }

    private fun normalizeIdentifier(
        identifier: String
    ): String {
        val normalizedIdentifier = identifier
            .trim()
            .uppercase()

        val identifierParts = normalizedIdentifier.split("-")

        if (identifierParts.size != 2) {
            return normalizedIdentifier
        }

        val prefix = identifierParts[0]
        val numericPart = identifierParts[1]

        if (numericPart.toIntOrNull() == null) {
            return normalizedIdentifier
        }

        val normalizedNumericPart = numericPart.padStart(
            length = requiredIdDigits,
            padChar = '0'
        )

        return "$prefix-$normalizedNumericPart"
    }

    private fun printInvalidPackageRowWarning(
        packageCsvRow: String
    ) {
        println("Warning: Invalid package row -> $packageCsvRow")
    }

    private fun printEmptyPackageFieldWarning(
        packageCsvRow: String
    ) {
        println("Warning: Empty required package field -> $packageCsvRow")
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
        ).joinToString(csvSeparator)
    }
}

