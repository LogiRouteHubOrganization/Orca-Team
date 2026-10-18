
package data.datasource.local.csv

import data.exception.local.LocalDataException
import data.parser.CsvRowParseResult
import data.validation.LocalDataSourceValidator
import java.io.File
import java.io.IOException
import java.util.logging.Logger

class CsvFileLoader(
    private val validator: LocalDataSourceValidator
) {

    fun <T> load(
        file: File,
        parseRow: (String) -> CsvRowParseResult<T>,
        headerLineCount: Int = 1
    ): List<T> {
        require(headerLineCount >= 0) {
            "Header line count cannot be negative."
        }

        validator.validate(file)

        val lines = try {
            file.readLines()
        } catch (exception: IOException) {
            throw LocalDataException(
                message = "Failed to read CSV file '${file.name}'.",
                cause = exception
            )
        }

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: '${file.name}'."
            )
        }

        val dataRows = lines.drop(headerLineCount)
        val validRecords = mutableListOf<T>()

        dataRows.forEachIndexed { index, row ->
            val lineNumber = index + headerLineCount + 1

            when (val result = parseRow(row)) {
                is CsvRowParseResult.Success -> {
                    validRecords.add(result.value)
                }

                is CsvRowParseResult.Failure -> {
                    logger.warning(
                        "Skipping invalid row in '${file.name}' " +
                                "at line $lineNumber: ${result.reason}"
                    )
                }
            }
        }

        if (validRecords.isEmpty()) {
            throw LocalDataException(
                "No valid data rows found in '${file.name}'. " +
                        "All ${dataRows.size} data rows are invalid."
            )
        }

        return validRecords
    }

    private companion object {
        val logger: Logger =
            Logger.getLogger(CsvFileLoader::class.java.name)
    }
}
