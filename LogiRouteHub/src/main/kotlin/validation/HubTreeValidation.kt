package validation

import domain.model.Warehouse
import domain.model.WarehouseLevel
import domain.tree.HubHierarchyInput
import domain.tree.HubLineageTracer
import domain.tree.HubTreeBuilder
import domain.usecasePackage.TraceHubLineageUseCase
private  val EXPECTED_HUB_LINEAGE_NODES = 3
private  val  FIRST_WAREHOUSE_Index=0
private  val SECOND_WAREHOUSE_Index=1
private  val THIRD_WAREHOUSE_Index=2
class HubTreeValidation(val warehouseIndex: Map<String, Warehouse>) {

    fun validateHubTree() {
    val warehouses = warehouseIndex.values.toList()
    check(warehouses.size >= EXPECTED_HUB_LINEAGE_NODES) { "Need at least 3 warehouses" }
    val hierarchy = listOf(HubHierarchyInput(warehouses[FIRST_WAREHOUSE_Index].id,
                WarehouseLevel.GLOBAL,
                null
            ),

            HubHierarchyInput(
                warehouses[SECOND_WAREHOUSE_Index].id,
                WarehouseLevel.REGIONAL,
                warehouses[FIRST_WAREHOUSE_Index].id
            ),

            HubHierarchyInput(
                warehouses[THIRD_WAREHOUSE_Index].id,
                WarehouseLevel.LOCAL,
                warehouses[SECOND_WAREHOUSE_Index].id
            )
        )

        val tree = HubTreeBuilder().build(
            warehouseIndex,
            hierarchy
        )

        check(tree != null) {
            "Hub Tree could not be built"
        }
        val localNode = tree.root
            .childNodes.first()
            .childNodes.first()
        val useCase = TraceHubLineageUseCase(
            HubLineageTracer()
        )
        val lineage = useCase(localNode)

        println("=== Hub Lineage Test ===")

        lineage.forEach {
            println("${it.warehouse.id} -> ${it.warehouseLevel}")
        }

        check(lineage.size == EXPECTED_HUB_LINEAGE_NODES)
        check(lineage.first().warehouseLevel == WarehouseLevel.LOCAL)
        check(lineage.last().warehouseLevel == WarehouseLevel.GLOBAL)

        println("Hub Tree Integration Test Passed!")
    }
}