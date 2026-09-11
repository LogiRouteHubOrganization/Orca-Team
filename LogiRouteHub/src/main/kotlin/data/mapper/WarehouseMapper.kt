package data.mapper

import data.dataholder.WarehouseRaw
import domain.model.Warehouse

class WarehouseMapper {

    fun mapToDomainModel(rawWarehouse: WarehouseRaw): Warehouse {
        return Warehouse(
            id = rawWarehouse.warehouseId,
            name = rawWarehouse.warehouseName,
            regionalZone = rawWarehouse.regionalZone,
            latitude = rawWarehouse.latitude,
            longitude = rawWarehouse.longitude
        )
    }
}
