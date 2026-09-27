package data.remote.mapper

import data.remote.dto.request.PackageCreateRequestDto
import data.remote.dto.request.PackageUpdateRequestDto
import data.remote.dto.request.WarehouseCreateRequestDto
import data.remote.dto.request.WarehouseUpdateRequestDto
import data.remote.dto.request.VehicleCreateRequestDto
import data.remote.dto.request.VehicleUpdateRequestDto
import data.remote.dto.request.RouteCreateRequestDto
import data.remote.dto.request.RouteUpdateRequestDto
import domain.model.Package
import domain.model.updateinput.PackageUpdateModel
import domain.model.Route
import domain.model.updateinput.RouteUpdateModel
import domain.model.Vehicle
import domain.model.updateinput.VehicleUpdateModel
import domain.model.Warehouse
import domain.model.updateinput.WarehouseUpdateModel


fun Package.toCreateDto(): PackageCreateRequestDto {
    return PackageCreateRequestDto(
        id = id,
        weightByKg = weightByKg,
        priority = priority,
        originWarehouseId = originWarehouse.id,
        destinationWarehouseId = destinationWarehouse.id
    )
}

fun PackageUpdateModel.toUpdateDto(): PackageUpdateRequestDto {
    return PackageUpdateRequestDto(
        weightByKg = weightByKg,
        priority = priority,
        originWarehouseId = originWarehouseId,
        destinationWarehouseId = destinationWarehouseId
    )
}


fun Warehouse.toCreateDto(): WarehouseCreateRequestDto {
    return WarehouseCreateRequestDto(
        id = id,
        name = name,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

fun WarehouseUpdateModel.toUpdateDto(): WarehouseUpdateRequestDto {
    return WarehouseUpdateRequestDto(
        name = name,
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

fun Vehicle.toCreateDto(): VehicleCreateRequestDto {
    return VehicleCreateRequestDto(
        id = id,
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm,
        currentWarehouseId = currentWarehouse.id
    )
}

fun VehicleUpdateModel.toUpdateDto(): VehicleUpdateRequestDto {
    return VehicleUpdateRequestDto(
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm,
        currentWarehouseId = currentWarehouseId
    )
}


fun Route.toCreateDto(): RouteCreateRequestDto {
    return RouteCreateRequestDto(
        id = id,
        originWarehouseId = originWarehouse.id,
        destinationWarehouseId = destinationWarehouse.id,
        distanceKm = distanceKm,
        typicalDelayMin = typicalDelayMin
    )
}

fun RouteUpdateModel.toUpdateDto(): RouteUpdateRequestDto {
    return RouteUpdateRequestDto(
        originWarehouseId = originWarehouseId,
        destinationWarehouseId = destinationWarehouseId,
        distanceKm = distanceKm,
        typicalDelayMin = typicalDelayMin
    )
}

