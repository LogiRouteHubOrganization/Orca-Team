package domain.validation

import domain.model.Warehouse
import domain.model.request.WarehouseLevel
import domain.tree.hierarchy.HubHierarchyInput
import domain.tree.hierarchy.HubLineageTracer
import domain.tree.hierarchy.HubTreeBuilder
import domain.usecasePackage.TraceHubLineageUseCase


class HubTreeValidation(private val warehouseIndex: Map<String, Warehouse>) {

    fun validateHubTree() {

        val expectedHubLineageNodes = 3
        val firstWarehouseIndex = 0
        val secondWarehouseIndex = 1
        val thirdWarehouseIndex = 2

        val warehouses = warehouseIndex.values.toList()
        check(warehouses.size >= expectedHubLineageNodes) {
            "Validation Failed: Need at least $expectedHubLineageNodes warehouses in warehouseIndex."
        }

        val hierarchy = listOf(
            HubHierarchyInput(
                warehouseId = warehouses[firstWarehouseIndex].id,
                warehouseLevel = WarehouseLevel.GLOBAL,
                parentWarehouseId = null
            ),
            HubHierarchyInput(
                warehouseId = warehouses[secondWarehouseIndex].id,
                warehouseLevel = WarehouseLevel.REGIONAL,
                parentWarehouseId = warehouses[firstWarehouseIndex].id
            ),
            HubHierarchyInput(
                warehouseId = warehouses[thirdWarehouseIndex].id,
                warehouseLevel = WarehouseLevel.LOCAL,
                parentWarehouseId = warehouses[secondWarehouseIndex].id
            )
        )

        val tree = HubTreeBuilder().build(warehouseIndex, hierarchy)
        checkNotNull(tree) { "Validation Failed: Hub Tree could not be built." }

        val regionalNode = tree.root.childNodes.firstOrNull()
            ?: error("Validation Failed: Regional node is missing.")

        val localNode = regionalNode.childNodes.firstOrNull()
            ?: error("Validation Failed: Local node is missing.")

        val traceUseCase = TraceHubLineageUseCase(HubLineageTracer())
        val lineage = traceUseCase(localNode)

        println("=== Hub Lineage Test ===")
        lineage.forEach { node ->
            println("${node.warehouse.id} -> ${node.warehouseLevel}")
        }

        check(lineage.size == expectedHubLineageNodes) {
            "Expected lineage size of $expectedHubLineageNodes, but got ${lineage.size}"
        }
        check(lineage.first().warehouseLevel == WarehouseLevel.LOCAL) {
            "Lineage path must start from LOCAL depot."
        }
        check(lineage.last().warehouseLevel == WarehouseLevel.GLOBAL) {
            "Lineage path must end at GLOBAL hub."
        }

        println("Hub Tree Integration Test Passed!")
    }
}
