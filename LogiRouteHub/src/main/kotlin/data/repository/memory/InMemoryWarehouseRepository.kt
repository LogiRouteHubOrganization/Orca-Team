package data.repository.memory

import data.datasource.WarehouseDataSource
import data.mapper.WarehouseMapper
import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.Warehouse
import domain.repository.WarehouseRepository

class InMemoryWarehouseRepository(
    private val warehouseDataSource: WarehouseDataSource,
    private val warehouseMapper: WarehouseMapper
) : WarehouseRepository {

    private val warehouses = mutableListOf<Warehouse>()

    init {
        loadWarehouses()
    }

    private fun loadWarehouses() {

        val rawWarehouses = warehouseDataSource.loadWarehouses()

        for (rawWarehouse in rawWarehouses) {

            val warehouse = warehouseMapper.map(rawWarehouse)

            warehouses.add(warehouse)
        }
    }

    override fun findById(warehouseId: String): Warehouse? {
        return warehouses.find { it.id == warehouseId }
    }

    override fun findAll(): List<Warehouse> {
        return warehouses
    }

    override fun update(warehouse: Warehouse) {

        val notFoundIndex = -1

        val warehouseIndex = warehouses.indexOfFirst {
            it.id == warehouse.id
        }

        if (warehouseIndex != notFoundIndex) {
            warehouses[warehouseIndex] = warehouse
        }
    }

    override fun addPackage(
        warehouse: Warehouse,
        logisticsPackage: Package
    ) {
        warehouse.addPackage(logisticsPackage)
    }

    override fun addVehicle(
        warehouse: Warehouse,
        vehicle: Vehicle
    ) {
        warehouse.addVehicle(vehicle)
    }

    override fun addRoute(
        warehouse: Warehouse,
        route: Route
    ) {
        warehouse.addRoute(route)
    }
}
