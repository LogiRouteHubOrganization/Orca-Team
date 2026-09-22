package domain.command.telemetry

import domain.command.CommandState

data class DispatchState(
    val vehiclePackageIds: List<String>,
    val warehouseQueuePackageIds: List<String>
) : CommandState {

    override fun describe(): String {
        return "Vehicle packages count: ${vehiclePackageIds.size}\n" +
                "Vehicle packages: ${vehiclePackageIds.take(5)}\n" +
                "Warehouse queue count: ${warehouseQueuePackageIds.size}\n" +
                "Warehouse queue: ${warehouseQueuePackageIds.take(5)}"
    }
}
