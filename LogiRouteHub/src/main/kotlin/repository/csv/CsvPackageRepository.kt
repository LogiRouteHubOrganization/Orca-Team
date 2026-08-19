package repository.csv

import dataholder.LogisticsPackageRaw
import parser.PackageParser
import repository.PackageRepository
import java.io.File


class CsvPackageRepository(
    private val packageFile: File,
    private val packageParser: PackageParser
) : PackageRepository {

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
