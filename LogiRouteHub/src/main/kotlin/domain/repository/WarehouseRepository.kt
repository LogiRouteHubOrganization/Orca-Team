package domain.repository

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.Warehouse

interface WarehouseRepository {

    fun findById(warehouseId: String): Warehouse?

    fun findAll(): List<Warehouse>

    fun update(warehouse: Warehouse)

    fun addPackage(
        warehouse: Warehouse,
        logisticsPackage: Package
    )

    fun addVehicle(
        warehouse: Warehouse,
        vehicle: Vehicle
    )

    fun addRoute(
        warehouse: Warehouse,
        route: Route
    )
}
