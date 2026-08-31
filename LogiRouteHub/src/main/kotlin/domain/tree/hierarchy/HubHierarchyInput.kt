package domain.tree.hierarchy

import domain.model.WarehouseLevel

data class HubHierarchyInput(
    val warehouseId: String,
    val warehouseLevel: WarehouseLevel,
    val parentWarehouseId: String?
)