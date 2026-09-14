package data.remote.mapper
import data.remote.dto.PackageDto
import data.remote.dto.RouteDto
import data.remote.dto.VehicleDto
import data.remote.dto.WarehouseDto
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