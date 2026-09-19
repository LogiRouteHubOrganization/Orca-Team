package data.datasource.csv

import data.dataholder.FleetRaw
import data.datasource.VehicleDataSource
import data.exception.LocalDataException
import data.parser.FleetParser
import data.validation.CsvFileValidator
import java.io.File

class CsvVehicleDataSource(
    private val vehicleFile: File,
    private val fleetParser: FleetParser,
    private val csvFileValidator: CsvFileValidator
) : VehicleDataSource {

    private val headerLineCount = 1

    override fun loadVehicles(): List<FleetRaw> {
        csvFileValidator.validate(vehicleFile)

        val lines = vehicleFile.readLines()

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${vehicleFile.name}"
            )
        }

        val dataRows = lines.drop(headerLineCount)

        return dataRows.mapIndexed { index, row ->
            fleetParser.parseFleetRow(row)
                ?: throw LocalDataException(
                    "Invalid vehicle row at line ${index + headerLineCount + 1}"
                )
        }
    }
}