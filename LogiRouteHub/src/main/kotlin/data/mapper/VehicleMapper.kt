package data.mapper

import data.dataholder.FleetRaw
import domain.model.Vehicle
import domain.model.Warehouse

class VehicleMapper {

    fun mapToDomainModel(
        rawVehicle: FleetRaw,
        currentWarehouse: Warehouse
    ): Vehicle {
        return Vehicle(
            id = rawVehicle.vehicleId,
            maxCapacityKg = rawVehicle.maxCapacityKg,
            costPerKm = rawVehicle.costPerKm,
            currentWarehouse = currentWarehouse
        )
    }
}
