package data.datasource.csv

import data.dataholder.LogisticsPackageRaw
import data.datasource.PackageDataSource
import data.exception.local.LocalDataException
import data.parser.PackageParser
import data.validation.LocalDataSourceValidator
import java.io.File

class CsvPackageDataSource(
    private val packageFile: File,
    private val packageParser: PackageParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : PackageDataSource {

    private val headerLineCount = 1

    override fun loadPackages(): List<LogisticsPackageRaw> {
        localDataSourceValidator.validate(packageFile)

        val packageRows = packageFile.readLines()

        if (packageRows.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${packageFile.name}"
            )
        }

        val dataRows = packageRows.drop(headerLineCount)

        return dataRows.mapNotNull { row ->
            packageParser.parsePackageRow(row)
        }
    }

    override fun addPackage(
        logisticsPackage: LogisticsPackageRaw
    ) {
        val packages = loadPackages().toMutableList()

        packages.add(logisticsPackage)

        writePackages(packages)
    }

    override fun updatePackage(
        logisticsPackage: LogisticsPackageRaw
    ) {
        val packages = loadPackages().toMutableList()

        val index = packages.indexOfFirst {
            it.packageId == logisticsPackage.packageId
        }

        if (index == -1) {
            throw LocalDataException(
                "Package not found: ${logisticsPackage.packageId}"
            )
        }

        packages[index] = logisticsPackage

        writePackages(packages)
    }

    override fun deletePackage(
        packageId: String
    ) {
        val packages = loadPackages().toMutableList()

        val removed = packages.removeIf {
            it.packageId == packageId
        }

        if (!removed) {
            throw LocalDataException(
                "Package not found: $packageId"
            )
        }

        writePackages(packages)
    }

    override fun replacePackages(
        packages: List<LogisticsPackageRaw>
    ) {
        writePackages(packages)
    }

    private fun writePackages(
        packages: List<LogisticsPackageRaw>
    ) {
        val header = "id,weight,originHubId,destinationHubId,priority"

        val rows = packages.map(
            packageParser::toCsvRow
        )

        val content = buildList {
            add(header)
            addAll(rows)
        }.joinToString(System.lineSeparator())

        packageFile.writeText(content)
    }
}
