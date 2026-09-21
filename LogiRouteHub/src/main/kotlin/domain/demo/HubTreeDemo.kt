package domain.demo

import domain.model.Warehouse
import domain.model.request.WarehouseLevel
import domain.tree.hierarchy.HubHierarchyInput
import domain.tree.hierarchy.HubLineageTracer
import domain.tree.hierarchy.HubTreeBuilder
import domain.usecasePackage.TraceHubLineageUseCase

class HubTreeDemo(
    private val warehouseIndex: Map<String, Warehouse>
) {

    fun runDemo() {
        val requiredWarehouseCount = 3

        val warehouses = warehouseIndex.values.toList()

        if (warehouses.size < requiredWarehouseCount) {
            println("Hub tree demo skipped.")
            println("At least $requiredWarehouseCount warehouses are required.")
            return
        }

        val globalWarehouse = warehouses[0]
        val regionalWarehouse = warehouses[1]
        val localWarehouse = warehouses[2]

        val hierarchy = listOf(
            HubHierarchyInput(
                warehouseId = globalWarehouse.id,
                warehouseLevel = WarehouseLevel.GLOBAL,
                parentWarehouseId = null
            ),
            HubHierarchyInput(
                warehouseId = regionalWarehouse.id,
                warehouseLevel = WarehouseLevel.REGIONAL,
                parentWarehouseId = globalWarehouse.id
            ),
            HubHierarchyInput(
                warehouseId = localWarehouse.id,
                warehouseLevel = WarehouseLevel.LOCAL,
                parentWarehouseId = regionalWarehouse.id
            )
        )

        val tree = HubTreeBuilder().build(
            warehouseIndex,
            hierarchy
        )

        if (tree == null) {
            println("Hub tree demo failed.")
            println("Hub tree could not be built.")
            return
        }

        val regionalNode = tree.root.childNodes.firstOrNull()

        if (regionalNode == null) {
            println("Hub tree demo failed.")
            println("Regional node was not found.")
            return
        }

        val localNode = regionalNode.childNodes.firstOrNull()

        if (localNode == null) {
            println("Hub tree demo failed.")
            println("Local node was not found.")
            return
        }

        val traceHubLineageUseCase = TraceHubLineageUseCase(
            HubLineageTracer()
        )

        val lineage = traceHubLineageUseCase(localNode)

        println()
        println("=== Hub Tree Demo ===")

        println("Hierarchy:")
        println("${globalWarehouse.id} -> ${WarehouseLevel.GLOBAL}")
        println("${regionalWarehouse.id} -> ${WarehouseLevel.REGIONAL}")
        println("${localWarehouse.id} -> ${WarehouseLevel.LOCAL}")

        println()
        println("Lineage from LOCAL to GLOBAL:")

        lineage.forEach { node ->
            println("${node.warehouse.id} -> ${node.warehouseLevel}")
        }
        println()
        println("Hub tree demo completed.")
    }
}
