package data.datasource.csv

import data.dataholder.LogisticsPackageRaw
import data.datasource.PackageDataSource
import data.exception.LocalDataException
import data.parser.PackageParser
import data.validation.CsvFileValidator
import java.io.File

class CsvPackageDataSource(
    private val packageFile: File,
    private val packageParser: PackageParser,
    private val csvFileValidator: CsvFileValidator
) : PackageDataSource {

    private val headerLineCount = 1

    override fun loadPackages(): List<LogisticsPackageRaw> {
        csvFileValidator.validate(packageFile)

        val packageRows = packageFile.readLines()

        if (packageRows.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${packageFile.name}"
            )
        }

        val dataRows = packageRows.drop(headerLineCount)

        return dataRows.mapIndexed { index, packageRow ->

            packageParser.parsePackageRow(packageRow)
                ?: throw LocalDataException(
                    "Invalid package row at line ${index + headerLineCount + 1}"
                )
        }
    }
}