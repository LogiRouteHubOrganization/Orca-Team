package domain.dispatcher

import domain.model.Vehicle

data class DispatchResult(
    val selectedVehicles: List<Vehicle>,
    val uncoveredZones: Set<String>
)
