package domain.demo

import domain.model.Warehouse
import domain.model.request.WarehouseLevel
import domain.tree.hierarchy.HubHierarchyInput
import domain.tree.hierarchy.HubLineageTracer
import domain.tree.hierarchy.HubTree
import domain.tree.hierarchy.HubTreeBuilder
import domain.tree.hierarchy.HubTreeWarehouseNode
import domain.usecase.TraceHubLineageUseCase

class HubTreeDemo(
    private val warehouseIndex: Map<String, Warehouse>
) {

    fun runDemo() {
        val requiredWarehouseCount = 3
        val warehouses = warehouseIndex.values.toList()

        if (warehouses.size < requiredWarehouseCount) {
            println("Hub tree demo skipped.\nAt least $requiredWarehouseCount warehouses are required.")
            return
        }

        val hierarchy = createHubHierarchy(warehouses)
        val tree = HubTreeBuilder().build(warehouseIndex, hierarchy) ?: run {
            println("Hub tree demo failed.\nHub tree could not be built.")
            return
        }

        val localNode = findLocalNode(tree) ?: return
        val traceHubLineageUseCase = TraceHubLineageUseCase(HubLineageTracer())
        val lineage = traceHubLineageUseCase(localNode)

        printDemoResults(warehouses, lineage)
    }

    private fun createHubHierarchy(warehouses: List<Warehouse>): List<HubHierarchyInput> {
        val global = warehouses[0]
        val regional = warehouses[1]
        val local = warehouses[2]

        return listOf(
            HubHierarchyInput(global.id, WarehouseLevel.GLOBAL, null),
            HubHierarchyInput(regional.id, WarehouseLevel.REGIONAL, global.id),
            HubHierarchyInput(local.id, WarehouseLevel.LOCAL, regional.id)
        )
    }

    private fun findLocalNode(tree: HubTree): HubTreeWarehouseNode? {
        val regionalNode = tree.root.childNodes.firstOrNull() ?: run {
            println("Hub tree demo failed.\nRegional node was not found.")
            return null
        }
        val localNode = regionalNode.childNodes.firstOrNull() ?: run {
            println("Hub tree demo failed.\nLocal node was not found.")
            return null
        }
        return localNode
    }

    private fun printDemoResults(
        warehouses: List<Warehouse>,
        lineage: List<HubTreeWarehouseNode>
    ) {
        println("\n=== Hub Tree Demo ===")
        println("Hierarchy:")
        println("${warehouses[0].id} -> ${WarehouseLevel.GLOBAL}")
        println("${warehouses[1].id} -> ${WarehouseLevel.REGIONAL}")
        println("${warehouses[2].id} -> ${WarehouseLevel.LOCAL}")
        println("\nLineage from LOCAL to GLOBAL:")

        lineage.forEach { node ->
            println("${node.warehouse.id} -> ${node.warehouseLevel}")
        }
        println("\nHub tree demo completed.")
    }
}


