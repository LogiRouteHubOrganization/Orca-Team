
package data.datasource.local.csv

import data.dataholder.LogisticsPackageRaw
import data.datasource.local.LocalPackageDataSource
import data.exception.local.LocalDataException
import data.parser.PackageParser
import dispatcher.AppDispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvPackageDataSourceImpl(
    private val packageFile: File,
    private val packageParser: PackageParser,
    private val csvFileLoader: CsvFileLoader,
    private val appDispatchers: AppDispatchers
) : LocalPackageDataSource {

    private companion object {
        const val HEADER_LINE_COUNT = 1
        const val CSV_HEADER =
            "id,weight,originHubId,destinationHubId,priority"
    }

    override suspend fun loadPackages(): List<LogisticsPackageRaw> =
        withContext(appDispatchers.io) {
            loadPackagesFromCsv()
        }

    override suspend fun addPackage(
        logisticsPackage: LogisticsPackageRaw
    ) {
        withContext(appDispatchers.io) {
            val packages = loadPackagesFromCsv().toMutableList()

            packages.add(logisticsPackage)

            writePackagesToCsv(packages)
        }
    }

    override suspend fun updatePackage(
        logisticsPackage: LogisticsPackageRaw
    ) {
        withContext(appDispatchers.io) {
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
        withContext(appDispatchers.io) {
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
        withContext(appDispatchers.io) {
            writePackagesToCsv(packages)
        }
    }

    private fun loadPackagesFromCsv(): List<LogisticsPackageRaw> =
        csvFileLoader.load(
            file = packageFile,
            parseRow = packageParser::parsePackageRow,
            headerLineCount = HEADER_LINE_COUNT
        )

    private fun writePackagesToCsv(
        packages: List<LogisticsPackageRaw>
    ) {
        val rows = packages.map(packageParser::toCsvRow)

        val content = buildList {
            add(CSV_HEADER)
            addAll(rows)
        }.joinToString(System.lineSeparator())

        packageFile.writeText(content)
    }
}
