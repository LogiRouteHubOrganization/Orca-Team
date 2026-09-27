package domain.usecase

import domain.tree.hierarchy.HubLineageTracer
import domain.tree.hierarchy.HubTreeWarehouseNode

class TraceHubLineageUseCase(
    private val hubLineageTracer: HubLineageTracer
) {
    operator fun invoke(
        localDepotNode: HubTreeWarehouseNode
    ): List<HubTreeWarehouseNode> {
        return hubLineageTracer.traceLineage(localDepotNode)
    }
}

