
package data.datasource.local.csv

import data.dataholder.FleetRaw
import data.datasource.local.LocalVehicleDataSource
import data.exception.local.LocalDataException
import data.parser.FleetParser
import data.validation.LocalDataSourceValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvVehicleDataSourceImpl(
    private val vehicleFile: File,
    private val fleetParser: FleetParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : LocalVehicleDataSource {

    private val headerLineCount = 1

    private val csvHeader =
        "vehicleId,currentHubId,maxCapacityKg,costPerKm"

    override suspend fun loadVehicles(): List<FleetRaw> =
        withContext(Dispatchers.IO) {
            loadVehiclesFromCsv()
        }

    override suspend fun addVehicle(vehicle: FleetRaw) {
        withContext(Dispatchers.IO) {
            val vehicles = loadVehiclesFromCsv().toMutableList()

            vehicles.add(vehicle)

            writeVehiclesToCsv(vehicles)
        }
    }

    override suspend fun updateVehicle(vehicle: FleetRaw) {
        withContext(Dispatchers.IO) {
            val vehicles = loadVehiclesFromCsv().toMutableList()

            val vehicleIndex = vehicles.indexOfFirst {
                it.vehicleId == vehicle.vehicleId
            }

            if (vehicleIndex == -1) {
                throw LocalDataException(
                    "Vehicle '${vehicle.vehicleId}' was not found in local data"
                )
            }

            vehicles[vehicleIndex] = vehicle

            writeVehiclesToCsv(vehicles)
        }
    }

    override suspend fun deleteVehicle(vehicleId: String) {
        withContext(Dispatchers.IO) {
            val vehicles = loadVehiclesFromCsv().toMutableList()

            val wasRemoved = vehicles.removeIf {
                it.vehicleId == vehicleId
            }

            if (!wasRemoved) {
                throw LocalDataException(
                    "Vehicle '$vehicleId' was not found in local data"
                )
            }

            writeVehiclesToCsv(vehicles)
        }
    }

    override suspend fun replaceVehicles(vehicles: List<FleetRaw>) {
        withContext(Dispatchers.IO) {
            writeVehiclesToCsv(vehicles)
        }
    }

    private fun loadVehiclesFromCsv(): List<FleetRaw> {
        localDataSourceValidator.validate(vehicleFile)

        val lines = vehicleFile.readLines()

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${vehicleFile.name}"
            )
        }

        return lines
            .drop(headerLineCount)
            .mapNotNull(fleetParser::parseFleetRow)
    }

    private fun writeVehiclesToCsv(
        vehicles: List<FleetRaw>
    ) {
        val content = buildList {
            add(csvHeader)

            vehicles.forEach { vehicle ->
                add(fleetParser.toCsvRow(vehicle))
            }
        }.joinToString("\n")

        vehicleFile.writeText(content)
    }
}
