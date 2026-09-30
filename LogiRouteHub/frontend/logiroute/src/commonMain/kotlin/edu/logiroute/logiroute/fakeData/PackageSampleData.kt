package edu.logiroute.logiroute.fakeData

import domain.model.Package

object PackageSampleData {

    val package1 = Package(
        id = "PKG-000017",
        weightByKg = 278.71,
        priority = "STANDARD",
        originWarehouse = WarehouseSampleData.warehouse1,
        destinationWarehouse = WarehouseSampleData.warehouse2
    )

    val package2 = Package(
        id = "PKG-000018",
        weightByKg = 315.63,
        priority = "URGENT",
        originWarehouse = WarehouseSampleData.warehouse1,
        destinationWarehouse = WarehouseSampleData.warehouse2
    )

    val all = listOf(package1, package2)
}