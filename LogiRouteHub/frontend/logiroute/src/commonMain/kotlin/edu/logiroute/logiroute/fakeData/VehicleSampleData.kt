package edu.logiroute.logiroute.fakeData

import domain.model.Vehicle

object VehicleSampleData {

    val vehicle1 = Vehicle(
        id = "TRK-000005",
        maxCapacityKg = 7572.5,
        costPerKm = 2.5,
        currentWarehouse = WarehouseSampleData.warehouse1
    )

    val vehicle2 = Vehicle(
        id = "TRK-000009",
        maxCapacityKg = 2572.5,
        costPerKm = 3.5,
        currentWarehouse = WarehouseSampleData.warehouse2
    )

    val all = listOf(vehicle1, vehicle2)
}