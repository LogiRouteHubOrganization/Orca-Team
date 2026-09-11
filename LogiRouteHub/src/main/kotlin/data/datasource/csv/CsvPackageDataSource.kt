package data.datasource.csv

import data.dataholder.LogisticsPackageRaw
import data.datasource.PackageDataSource
import data.parser.PackageParser
import java.io.File


class CsvPackageDataSource(
    private val packageFile: File,
    private val packageParser: PackageParser
) : PackageDataSource {

    private val headerLineCount = 1

    override fun loadPackages(): List<LogisticsPackageRaw> {
        if (!packageFile.exists()) {
            println("Diagnostic Warning: File ${packageFile.name} does not exist.")
            return emptyList()
        }
        val packageRows = packageFile.readLines()
        val dataRows = packageRows.drop(headerLineCount)
        val validPackages = mutableListOf<LogisticsPackageRaw>()

        for (packageRow in dataRows) {
            val parsedPackage = packageParser.parsePackageRow(packageRow)
            if (parsedPackage != null) {
                validPackages.add(parsedPackage)
            }
        }
        return validPackages
    }
}
