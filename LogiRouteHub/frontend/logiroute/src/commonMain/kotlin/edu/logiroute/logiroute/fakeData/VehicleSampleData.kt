package edu.logiroute.logiroute.fakeData

import domain.model.Vehicle

object VehicleSampleData {

    val vehicle1 = Vehicle(
        id = "TRK-000005",
        maxCapacityKg = 10000.0,
        costPerKm = 2.5,
        currentWarehouse = WarehouseSampleData.warehouse1
    )

    val vehicle2 = Vehicle(
        id = "TRK-000009",
        maxCapacityKg = 10000.0,
        costPerKm = 3.5,
        currentWarehouse = WarehouseSampleData.warehouse2
    )

    val vehicle3 = Vehicle(
        id = "TRK-000008",
        maxCapacityKg = 10000.0,
        costPerKm = 3.5,
        currentWarehouse = WarehouseSampleData.warehouse3
    )

    val AllVehicle = listOf(vehicle1, vehicle2,vehicle3)
}