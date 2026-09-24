package edu.logiroute.logiroute.previews

import domain.model.Package
import domain.model.Vehicle
import domain.model.Warehouse

object SampleData {
        val dummyWarehouse1 = Warehouse(
            id = "WH-047",
            name = "Hub-047",
            regionalZone = "CENTRAL",
            latitude = 43.44,
            longitude = -100.65
        ).apply {
            addPackage(
                Package(
                    id = "PKG-000017",
                    weightByKg = 278.71,
                    priority = "standard",
                    originWarehouse = this,
                    destinationWarehouse = this
                )
            )
            addVehicle(
                Vehicle(
                    id="TRK-0005",
                    maxCapacityKg=7572.5,
                    costPerKm=2.5,
                    currentWarehouse=this
                )
            )
        }
        val dummyWarehouse2 = Warehouse(
            id = "WH-067",
            name = "Hub-067",
            regionalZone = "NORTH",
            latitude = 48.72,
            longitude = -114.37
        ).apply {
            addPackage(
                Package(
                    id = "pkg-000018",
                    weightByKg = 315.63,
                    priority = "URGENT",
                    originWarehouse = this,
                    destinationWarehouse = this
                )
            )
            addVehicle(
                Vehicle(
                    id="TRK-0009",
                    maxCapacityKg=2572.5,
                    costPerKm=3.5,
                    currentWarehouse=this
                )
            )
        }
        val dummyWarehouse3 = Warehouse(
            id = "WH-056",
            name = "Hub-056",
            regionalZone = "SOUTH",
            latitude = 39.38,
            longitude = -91.06
        )
    val sampleWarehouses = listOf(dummyWarehouse1, dummyWarehouse2, dummyWarehouse3)


}