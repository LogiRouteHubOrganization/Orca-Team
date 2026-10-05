package domain.algorithm.greedy

import domain.model.Vehicle

data class DispatchResult(
    val selectedVehicles: List<Vehicle>,
    val uncoveredZones: Set<String>
)
