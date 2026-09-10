package data.repository.memory

import domain.model.Warehouse
import domain.repository.WarehouseRepository

class InMemoryWarehouseRepository(
    private val warehouses: MutableList<Warehouse> = mutableListOf()
) : WarehouseRepository {

    override fun findById(warehouseId: String): Warehouse? {
        return warehouses.find { it.id == warehouseId }
    }

    override fun findAll(): List<Warehouse> {
        return warehouses
    }

    override fun add(warehouse: Warehouse) {
        warehouses.add(warehouse)
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
}
