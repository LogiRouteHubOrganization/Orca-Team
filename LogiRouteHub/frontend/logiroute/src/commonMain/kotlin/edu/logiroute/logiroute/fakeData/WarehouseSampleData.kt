package edu.logiroute.logiroute.fakeData

import domain.model.Warehouse

object WarehouseSampleData {

    val warehouse1 = Warehouse(
        id = "WH-000047",
        name = "Rafah Cross-Dock Facility",
        regionalZone = "CENTRAL",
        latitude = 43.44,
        longitude = -100.65
    )

    val warehouse2 = Warehouse(
        id = "WH-000067",
        name = "Khan Younis Logistics Terminal",
        regionalZone = "NORTH",
        latitude = 48.72,
        longitude = -114.37
    )

    val warehouse3 = Warehouse(
        id = "WH-000056",
        name = "North District Fulfillment Hub",
        regionalZone = "SOUTH",
        latitude = 39.38,
        longitude = -91.06
    )

    val AllWarehouse = listOf(warehouse1, warehouse2, warehouse3)
}