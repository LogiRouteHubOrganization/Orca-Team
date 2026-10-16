package domain.algorithm.tree.hierarchy

import domain.model.request.WarehouseLevel

class HubLineageTracer {

    fun traceLineage(
        localDepotNode: HubTreeWarehouseNode
    ): List<HubTreeWarehouseNode> {
        if (localDepotNode.warehouseLevel != WarehouseLevel.LOCAL) {
            return emptyList()
        }
        return traceToGlobalHub(localDepotNode)
    }

    private fun traceToGlobalHub(
        currentNode: HubTreeWarehouseNode
    ): List<HubTreeWarehouseNode> {
        if (currentNode.warehouseLevel == WarehouseLevel.GLOBAL) {
            return listOf(currentNode)
        }

        val parentNode = currentNode.warehouseParent ?: return emptyList()

        val parentLineage = traceToGlobalHub(parentNode)

        if (parentLineage.isEmpty()) {
            return emptyList()
        }
        return listOf(currentNode) + parentLineage
    }
}
