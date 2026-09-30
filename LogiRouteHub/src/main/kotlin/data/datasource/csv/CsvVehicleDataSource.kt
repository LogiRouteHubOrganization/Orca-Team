package data.datasource.csv

import data.dataholder.FleetRaw
import data.datasource.VehicleDataSource
import data.exception.local.LocalDataException
import data.parser.FleetParser
import data.validation.LocalDataSourceValidator
import java.io.File

class CsvVehicleDataSource(
    private val vehicleFile: File,
    private val fleetParser: FleetParser,
    private val localDataSourceValidator: LocalDataSourceValidator
) : VehicleDataSource {

    private val headerLineCount = 1

    override fun loadVehicles(): List<FleetRaw> {
        localDataSourceValidator.validate(vehicleFile)

        val lines = vehicleFile.readLines()

        if (lines.size <= headerLineCount) {
            throw LocalDataException(
                "CSV file contains no data rows: ${vehicleFile.name}"
            )
        }

        val dataRows = lines.drop(headerLineCount)

        return dataRows.mapNotNull { row ->
            fleetParser.parseFleetRow(row)
        }
    }

    override fun addVehicle(vehicle: FleetRaw) {
        val vehicles = loadVehicles().toMutableList()

        vehicles.add(vehicle)

        writeVehicles(vehicles)
    }

    override fun updateVehicle(vehicle: FleetRaw) {
        val vehicles = loadVehicles().toMutableList()

        val vehicleIndex = vehicles.indexOfFirst {
            it.vehicleId == vehicle.vehicleId
        }

        if (vehicleIndex == -1) {
            throw LocalDataException(
                "Vehicle '${vehicle.vehicleId}' was not found in local data"
            )
        }

        vehicles[vehicleIndex] = vehicle

        writeVehicles(vehicles)
    }

    override fun deleteVehicle(vehicleId: String) {
        val vehicles = loadVehicles().toMutableList()

        val wasRemoved = vehicles.removeIf {
            it.vehicleId == vehicleId
        }

        if (!wasRemoved) {
            throw LocalDataException(
                "Vehicle '$vehicleId' was not found in local data"
            )
        }

        writeVehicles(vehicles)
    }

    override fun replaceVehicles(
        vehicles: List<FleetRaw>
    ) {
        writeVehicles(vehicles)
    }

    private fun writeVehicles(
        vehicles: List<FleetRaw>
    ) {
        val header = "vehicleId,currentHubId,maxCapacityKg,costPerKm"

        val content = buildList {
            add(header)

            vehicles.forEach { vehicle ->
                add(
                    fleetParser.toCsvRow(vehicle)
                )
            }
        }.joinToString("\n")

        vehicleFile.writeText(content)
    }
}
