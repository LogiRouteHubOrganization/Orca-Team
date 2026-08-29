package domain.tree

import domain.model.Warehouse
import domain.model.WarehouseLevel

class HubTreeBuilder {
    private val requiredGlobalHubCount = 1

    fun build(
        warehouses: Map<String, Warehouse>,
        hierarchyInput: List<HubHierarchyInput>
    ): HubTree? {

        val globalHubs = hierarchyInput.filter {
            it.warehouseLevel == WarehouseLevel.GLOBAL
        }

        if (globalHubs.size != requiredGlobalHubCount) return null

        val rootInput = globalHubs.first()

        if (rootInput.parentWarehouseId != null) return null

        val hasNodeWithoutRequiredParent = hierarchyInput.any { input ->
            input.warehouseLevel != WarehouseLevel.GLOBAL &&
                    input.parentWarehouseId == null
        }

        if (hasNodeWithoutRequiredParent) return null


        val nodesByWarehouseId = hierarchyInput.associate { input ->
            input.warehouseId to HubTreeWarehouseNode(
                warehouse = warehouses[input.warehouseId] ?: return null,
                warehouseLevel = input.warehouseLevel
            )
        }

        hierarchyInput.filter { it.parentWarehouseId != null }
            .forEach { input ->
                val childNode = nodesByWarehouseId[input.warehouseId] ?: return null

                val parentNode = nodesByWarehouseId[input.parentWarehouseId] ?: return null

                if (!isValidRelationship(parentNode, childNode)) return null

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
            WarehouseLevel.GLOBAL ->
                child.warehouseLevel == WarehouseLevel.REGIONAL

            WarehouseLevel.REGIONAL ->
                child.warehouseLevel == WarehouseLevel.LOCAL

            WarehouseLevel.LOCAL ->
                false
        }
    }
}
