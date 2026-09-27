package domain.command.telemetry

import domain.command.CommandState

data class PackageState(
    val packageId: String?,
    val destinationWarehouseId: String?,
    val index: Int?
) : CommandState {

    override fun describe(): String {
        return "Package: $packageId\n" +
                "Destination: $destinationWarehouseId\n" +
                "Index: $index"
    }
}
