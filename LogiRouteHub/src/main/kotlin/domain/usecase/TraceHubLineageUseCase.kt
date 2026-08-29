package domain.usecasePackage

import domain.tree.HubLineageTracer
import domain.tree.HubTreeWarehouseNode

class TraceHubLineageUseCase(
    private val hubLineageTracer: HubLineageTracer
) {
    operator fun invoke(
        localDepotNode: HubTreeWarehouseNode
    ): List<HubTreeWarehouseNode> {
        return hubLineageTracer.traceLineage(localDepotNode)
    }
}
