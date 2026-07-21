package parser

import model.LogisticsPackage
import java.io.File

class PackageParser {

    fun parse(filePath: String): List<LogisticsPackage> {
        val packageLines = readPackageLines(filePath)

        return packageLines
            .drop(1)
            .mapNotNull(::createPackage)
    }

    private fun readPackageLines(filePath: String): List<String> {
        return File(filePath).readLines()
    }

    private fun createPackage(line: String): LogisticsPackage? {
        val columns = line.split(COLUMN_SEPARATOR)

        if (columns.size != EXPECTED_COLUMN_COUNT) {
            return null
        }

        return LogisticsPackage(
            id = columns[ID_INDEX].trim(),
            weight = columns[WEIGHT_INDEX].trim(),
            destinationHubId = columns[DESTINATION_HUB_INDEX].trim(),
            priority = columns[PRIORITY_INDEX].trim()
        )
    }

    companion object {
        private const val COLUMN_SEPARATOR = ","
        private const val EXPECTED_COLUMN_COUNT = 4

        private const val ID_INDEX = 0
        private const val WEIGHT_INDEX = 1
        private const val DESTINATION_HUB_INDEX = 2
        private const val PRIORITY_INDEX = 3
    }
}