package domain.algorithm.tree.hierarchy

import domain.model.Warehouse
import domain.model.request.WarehouseLevel
import kotlin.collections.get

class HubTreeBuilder {
    private val requiredGlobalHubCount = 1

    fun build(
        warehouses: Map<String, Warehouse>,
        hierarchyInput: List<HubHierarchyInput>
    ): HubTree? {
        val globalHubs = hierarchyInput.filter { it.warehouseLevel == WarehouseLevel.GLOBAL }
        if (globalHubs.size != requiredGlobalHubCount || globalHubs.first().parentWarehouseId != null) {
            return null
        }

        val rootInput = globalHubs.first()

        val hasNodeWithoutRequiredParent = hierarchyInput.any { input ->
            input.warehouseLevel != WarehouseLevel.GLOBAL && input.parentWarehouseId == null
        }
        if (hasNodeWithoutRequiredParent) return null

        val nodesByWarehouseId = mutableMapOf<String, HubTreeWarehouseNode>()
        for (input in hierarchyInput) {
            val warehouse = warehouses[input.warehouseId] ?: return null
            nodesByWarehouseId[input.warehouseId] = HubTreeWarehouseNode(
                warehouse = warehouse,
                warehouseLevel = input.warehouseLevel
            )
        }

        for (input in hierarchyInput.filter { it.parentWarehouseId != null }) {
            val childNode = nodesByWarehouseId[input.warehouseId] ?: return null
            val parentNode = nodesByWarehouseId[input.parentWarehouseId] ?: return null

            if (!isValidRelationship(parentNode, childNode)) {
                return null
            }
            parentNode.addChild(childNode)
        }

        val rootNode = nodesByWarehouseId[rootInput.warehouseId] ?: return null
        return HubTree(rootNode)
    }

    private fun isValidRelationship(
        parent: HubTreeWarehouseNode,
        child: HubTreeWarehouseNode
    ): Boolean {
        return when (parent.warehouseLevel) {
            WarehouseLevel.GLOBAL -> child.warehouseLevel == WarehouseLevel.REGIONAL
            WarehouseLevel.REGIONAL -> child.warehouseLevel == WarehouseLevel.LOCAL
            WarehouseLevel.LOCAL -> false
        }
    }
}
