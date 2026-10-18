
package data.datasource.local.csv

import data.dataholder.FleetRaw
import data.datasource.local.LocalVehicleDataSource
import data.exception.local.LocalDataException
import data.parser.FleetParser
import dispatcher.AppDispatchers
import kotlinx.coroutines.withContext
import java.io.File

class CsvVehicleDataSourceImpl(
    private val vehicleFile: File,
    private val fleetParser: FleetParser,
    private val csvFileLoader: CsvFileLoader,
    private val appDispatchers: AppDispatchers
) : LocalVehicleDataSource {

    private companion object {
        const val HEADER_LINE_COUNT = 1
        const val CSV_HEADER =
            "vehicleId,currentHubId,maxCapacityKg,costPerKm"
    }

    override suspend fun loadVehicles(): List<FleetRaw> =
        withContext(appDispatchers.io) {
            loadVehiclesFromCsv()
        }

    override suspend fun addVehicle(vehicle: FleetRaw) {
        withContext(appDispatchers.io) {
            val vehicles = loadVehiclesFromCsv().toMutableList()
            vehicles.add(vehicle)
            writeVehiclesToCsv(vehicles)
        }
    }

    override suspend fun updateVehicle(vehicle: FleetRaw) {
        withContext(appDispatchers.io) {
            val vehicles = loadVehiclesFromCsv().toMutableList()

            val index = vehicles.indexOfFirst {
                it.vehicleId == vehicle.vehicleId
            }

            if (index == -1) {
                throw LocalDataException(
                    "Vehicle '${vehicle.vehicleId}' was not found in local data."
                )
            }

            vehicles[index] = vehicle
            writeVehiclesToCsv(vehicles)
        }
    }

    override suspend fun deleteVehicle(vehicleId: String) {
        withContext(appDispatchers.io) {
            val vehicles = loadVehiclesFromCsv().toMutableList()

            val removed = vehicles.removeIf {
                it.vehicleId == vehicleId
            }

            if (!removed) {
                throw LocalDataException(
                    "Vehicle '$vehicleId' was not found in local data."
                )
            }

            writeVehiclesToCsv(vehicles)
        }
    }

    override suspend fun replaceVehicles(vehicles: List<FleetRaw>) {
        withContext(appDispatchers.io) {
            writeVehiclesToCsv(vehicles)
        }
    }

    private fun loadVehiclesFromCsv(): List<FleetRaw> =
        csvFileLoader.load(
            file = vehicleFile,
            parseRow = fleetParser::parseFleetRow,
            headerLineCount = HEADER_LINE_COUNT
        )

    private fun writeVehiclesToCsv(vehicles: List<FleetRaw>) {
        val content = buildList {
            add(CSV_HEADER)
            addAll(vehicles.map(fleetParser::toCsvRow))
        }.joinToString(System.lineSeparator())

        vehicleFile.writeText(content)
    }
}
