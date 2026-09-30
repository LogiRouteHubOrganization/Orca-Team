package data.remote.mapper
import data.dataholder.FleetRaw
import data.dataholder.LogisticsPackageRaw
import data.dataholder.WarehouseRaw
import data.remote.dto.response.PackageDto
import data.remote.dto.response.RouteDto
import data.remote.dto.response.VehicleDto
import data.remote.dto.response.WarehouseDto
import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.Warehouse

fun WarehouseDto.toDomain(): Warehouse {
    return Warehouse(
        id = id,
        name = name,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

fun RouteDto.toDomain(
    originWarehouse: Warehouse,
    destinationWarehouse: Warehouse
): Route {
    return Route(
        id = id,
        originWarehouse = originWarehouse,
        destinationWarehouse = destinationWarehouse,
        distanceKm = distanceKm,
        typicalDelayMin = typicalDelayMin
    )
}

fun VehicleDto.toDomain(
    currentWarehouse: Warehouse
): Vehicle {
    return Vehicle(
        id = id,
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm,
        currentWarehouse = currentWarehouse
    )
}

fun PackageDto.toDomain(
    originWarehouse: Warehouse,
    destinationWarehouse: Warehouse
): Package {
    return Package(
        id = id,
        weightByKg = weightByKg,
        priority = priority,
        originWarehouse = originWarehouse,
        destinationWarehouse = destinationWarehouse
    )
}
fun WarehouseDto.toRaw(): WarehouseRaw {
    return WarehouseRaw(
        warehouseId = id,
        warehouseName = name,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

fun PackageDto.toRaw(): LogisticsPackageRaw {
    return LogisticsPackageRaw(
        packageId = id,
        packageWeightByKg = weightByKg,
        originHubId = originWarehouseId,
        destinationHubId = destinationWarehouseId,
        packagePriority = priority
    )
}
fun VehicleDto.toRaw(): FleetRaw {
    return FleetRaw(
        vehicleId = id,
        currentHubId = currentWarehouseId,
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm
    )
}
