package edu.logiroute.logiroute.fakeData
import domain.model.Route

object RouteSampleData {
    val route1=Route(
        id="RT-000001",
        originWarehouse=WarehouseSampleData.warehouse1,
        destinationWarehouse=WarehouseSampleData.warehouse2,
        distanceKm=12.5,
        typicalDelayMin=50
        )
    val route2=Route(
        id="RT-000002",
        originWarehouse=WarehouseSampleData.warehouse2,
        destinationWarehouse=WarehouseSampleData.warehouse1,
        distanceKm=1000.3,
        typicalDelayMin=130
    )
    val route3=Route(
        id="RT-000004",
        originWarehouse=WarehouseSampleData.warehouse3,
        destinationWarehouse=WarehouseSampleData.warehouse1,
        distanceKm=122.5,
        typicalDelayMin=50
    )
    val AllRoute=listOf(route1,route2,route3)
}