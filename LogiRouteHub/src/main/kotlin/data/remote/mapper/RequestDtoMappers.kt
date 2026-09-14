package data.remote.mapper

import data.remote.dto.request.RouteRequestDto
import data.remote.dto.request.WarehouseRequestDto
import domain.model.Route
import domain.model.Warehouse
import data.remote.dto.request.VehicleRequestDto
import domain.model.Vehicle
import data.remote.dto.request.PackageRequestDto
import domain.model.Package

fun Warehouse.toRequestDto(): WarehouseRequestDto {
    return WarehouseRequestDto(
        name = name,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

fun Route.toRequestDto(): RouteRequestDto {
    return RouteRequestDto(
        originWarehouseId = originWarehouse.id,
        destinationWarehouseId = destinationWarehouse.id,
        distanceKm = distanceKm,
        typicalDelayMin = typicalDelayMin
    )
}

fun Vehicle.toRequestDto(): VehicleRequestDto {
    return VehicleRequestDto(
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm,
        currentWarehouseId = currentWarehouse.id
    )
}

fun Package.toRequestDto(): PackageRequestDto {
    return PackageRequestDto(
        weightByKg = weightByKg,
        priority = priority,
        originWarehouseId = originWarehouse.id,
        destinationWarehouseId = destinationWarehouse.id
    )
}