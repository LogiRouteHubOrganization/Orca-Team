package edu.logiroute.logiroute.fakeData

import domain.model.Warehouse

object WarehouseSampleData {

    val warehouse1 = Warehouse(
        id = "WH-000047",
        name = "Hub-000047",
        regionalZone = "CENTRAL",
        latitude = 43.44,
        longitude = -100.65
    )

    val warehouse2 = Warehouse(
        id = "WH-000067",
        name = "Hub-000067",
        regionalZone = "NORTH",
        latitude = 48.72,
        longitude = -114.37
    )

    val warehouse3 = Warehouse(
        id = "WH-000056",
        name = "Hub-000056",
        regionalZone = "SOUTH",
        latitude = 39.38,
        longitude = -91.06
    )

    val all = listOf(warehouse1, warehouse2, warehouse3)
}