package data.repository.memory


import domain.model.Warehouse
import domain.model.Package
import domain.repository.WarehouseRepository

class InMemoryWarehouseRepository(
    private val warehouses: MutableList<Warehouse>
) : WarehouseRepository {

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
}
