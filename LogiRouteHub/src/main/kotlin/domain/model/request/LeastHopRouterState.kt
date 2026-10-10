package domain.model.request

import domain.model.Warehouse

data class LeastHopRouterState (
    val queue: ArrayDeque<Warehouse>,
    val visitedWarehouseIds: Set<String>,
    val previousWarehouseById: Map<String, Warehouse>
)


