package domain.algorithm.tree.hierarchy

import domain.model.request.WarehouseLevel

data class HubHierarchyInput(
    val warehouseId: String,
    val warehouseLevel: WarehouseLevel,
    val parentWarehouseId: String?
)
