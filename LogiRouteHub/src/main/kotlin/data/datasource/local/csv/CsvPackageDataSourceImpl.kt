
package data.datasource.local.csv

import data.dataholder.LogisticsPackageRaw
import data.datasource.local.LocalPackageDataSource
import data.exception.local.LocalDataException
import data.parser.PackageParser
import data.validation.LocalDataSourceValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvPackageDataSourceImpl(
    private val packageFile: File,
    private val packageParser: PackageParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : LocalPackageDataSource {

    private val headerLineCount = 1

    private val csvHeader =
        "id,weight,originHubId,destinationHubId,priority"

    override suspend fun loadPackages(): List<LogisticsPackageRaw> =
        withContext(Dispatchers.IO) {
            loadPackagesFromCsv()
        }

    override suspend fun addPackage(
        logisticsPackage: LogisticsPackageRaw
    ) {
        withContext(Dispatchers.IO) {
            val packages = loadPackagesFromCsv().toMutableList()

            packages.add(logisticsPackage)

            writePackagesToCsv(packages)
        }
    }

    override suspend fun updatePackage(
        logisticsPackage: LogisticsPackageRaw
    ) {
        withContext(Dispatchers.IO) {
            val packages = loadPackagesFromCsv().toMutableList()

            val index = packages.indexOfFirst {
                it.packageId == logisticsPackage.packageId
            }

            if (index == -1) {
                throw LocalDataException(
                    "Package not found: ${logisticsPackage.packageId}"
                )
            }

            packages[index] = logisticsPackage

            writePackagesToCsv(packages)
        }
    }

    override suspend fun deletePackage(packageId: String) {
        withContext(Dispatchers.IO) {
            val packages = loadPackagesFromCsv().toMutableList()

            val removed = packages.removeIf {
                it.packageId == packageId
            }

            if (!removed) {
                throw LocalDataException(
                    "Package not found: $packageId"
                )
            }
            writePackagesToCsv(packages)
        }
    }

    override suspend fun replacePackages(
        packages: List<LogisticsPackageRaw>
    ) {
        withContext(Dispatchers.IO) {
            writePackagesToCsv(packages)
        }
    }

    private fun loadPackagesFromCsv(): List<LogisticsPackageRaw> {
        localDataSourceValidator.validate(packageFile)

        val packageRows = packageFile.readLines()

        if (packageRows.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${packageFile.name}"
            )
        }

        return packageRows
            .drop(headerLineCount)
            .mapNotNull(packageParser::parsePackageRow)
    }

    private fun writePackagesToCsv(
        packages: List<LogisticsPackageRaw>
    ) {
        val rows = packages.map(packageParser::toCsvRow)

        val content = buildList {
            add(csvHeader)
            addAll(rows)
        }.joinToString(System.lineSeparator())

        packageFile.writeText(content)
    }
}
