package domain.algorithm.tree.hierarchy

import domain.model.Warehouse
import domain.model.request.WarehouseLevel

class HubTreeWarehouseNode(
    val warehouse: Warehouse,
    val warehouseLevel: WarehouseLevel
) {

    var warehouseParent: HubTreeWarehouseNode? = null

    private val children = mutableListOf<HubTreeWarehouseNode>()

    val childNodes: List<HubTreeWarehouseNode>
        get() = children

    fun addChild(child: HubTreeWarehouseNode) {
        children.add(child)
        child.warehouseParent = this
    }
}
