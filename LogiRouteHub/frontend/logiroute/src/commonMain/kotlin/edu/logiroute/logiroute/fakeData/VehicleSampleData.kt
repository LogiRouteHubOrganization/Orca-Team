package edu.logiroute.logiroute.fakeData

import domain.model.Vehicle

object VehicleSampleData {

    val vehicle1 = Vehicle(
        id = "TRK-000001",
        maxCapacityKg = 12000.0,
        costPerKm = 2.8,
        currentWarehouse = WarehouseSampleData.warehouse1
    )

    val vehicle2 = Vehicle(
        id = "TRK-000002",
        maxCapacityKg = 8500.0,
        costPerKm = 2.2,
        currentWarehouse = WarehouseSampleData.warehouse2
    )

    val vehicle3 = Vehicle(
        id = "TRK-000003",
        maxCapacityKg = 15000.0,
        costPerKm = 3.6,
        currentWarehouse = WarehouseSampleData.warehouse3
    )

    val vehicle4 = Vehicle(
        id = "TRK-000004",
        maxCapacityKg = 6000.0,
        costPerKm = 1.9,
        currentWarehouse = WarehouseSampleData.warehouse4
    )

    val vehicle5 = Vehicle(
        id = "TRK-000005",
        maxCapacityKg = 10000.0,
        costPerKm = 2.5,
        currentWarehouse = WarehouseSampleData.warehouse5
    )

    val vehicle6 = Vehicle(
        id = "TRK-000006",
        maxCapacityKg = 14000.0,
        costPerKm = 3.2,
        currentWarehouse = WarehouseSampleData.warehouse6
    )

    val vehicle7 = Vehicle(
        id = "TRK-000007",
        maxCapacityKg = 7500.0,
        costPerKm = 2.0,
        currentWarehouse = WarehouseSampleData.warehouse7
    )

    val vehicle8 = Vehicle(
        id = "TRK-000008",
        maxCapacityKg = 11000.0,
        costPerKm = 2.9,
        currentWarehouse = WarehouseSampleData.warehouse8
    )

    val vehicle9 = Vehicle(
        id = "TRK-000009",
        maxCapacityKg = 9500.0,
        costPerKm = 2.4,
        currentWarehouse = WarehouseSampleData.warehouse9
    )

    val vehicle10 = Vehicle(
        id = "TRK-000010",
        maxCapacityKg = 1.0,
        costPerKm = 3.1,
        currentWarehouse = WarehouseSampleData.warehouse22
    )

    val vehicle11 = Vehicle(
        id = "TRK-000011",
        maxCapacityKg = 5000.0,
        costPerKm = 1.6,
        currentWarehouse = WarehouseSampleData.warehouse12
    )

    val vehicle12 = Vehicle(
        id = "TRK-000012",
        maxCapacityKg = 16000.0,
        costPerKm = 3.8,
        currentWarehouse = WarehouseSampleData.warehouse50
    )

    val AllVehicle = listOf(
        vehicle1,
        vehicle2,
        vehicle3,
        vehicle4,
        vehicle5,
        vehicle6,
        vehicle7,
        vehicle8,
        vehicle9,
        vehicle10,
        vehicle11,
        vehicle12
    )
}