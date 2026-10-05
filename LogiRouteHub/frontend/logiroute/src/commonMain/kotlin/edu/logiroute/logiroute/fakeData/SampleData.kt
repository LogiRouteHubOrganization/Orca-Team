package edu.logiroute.logiroute.fakeData

import domain.model.Warehouse

object SampleData {

    val sampleWarehouses: List<Warehouse> by lazy {
        WarehouseSampleData.warehouse1.apply {
            addPackage(PackageSampleData.package1)
            addVehicle(VehicleSampleData.vehicle1)
        }
        WarehouseSampleData.warehouse2.apply {
            addPackage(PackageSampleData.package2)
            addVehicle(VehicleSampleData.vehicle2)
        }
        WarehouseSampleData.AllWarehouse
    }
}