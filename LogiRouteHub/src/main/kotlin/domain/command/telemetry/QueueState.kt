package domain.command.telemetry

import domain.command.CommandState

data class QueueState(
    val packageIds: List<String>
) : CommandState {

    override fun describe(): String {
        return "Queue count: ${packageIds.size}\n" +
                "First packages: ${packageIds.take(5)}"
    }
}
