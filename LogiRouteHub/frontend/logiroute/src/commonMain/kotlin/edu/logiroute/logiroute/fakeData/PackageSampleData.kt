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

    val package3 = Package(
        id = "PKG-000019",
        weightByKg = 50000.50,
        priority = "STANDARD",
        originWarehouse = WarehouseSampleData.warehouse12,
        destinationWarehouse = WarehouseSampleData.warehouse22
    )
    val package4 = Package(
        id = "PKG-000020",
        weightByKg = 160500.0,
        priority = "URGENT",
        originWarehouse = WarehouseSampleData.warehouse15,
        destinationWarehouse = WarehouseSampleData.warehouse35
    )


    val AllPackage = listOf(package1, package2,package3,package4)
}