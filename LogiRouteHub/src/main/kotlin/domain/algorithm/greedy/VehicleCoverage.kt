package domain.algorithm.greedy

import domain.model.Vehicle

data class VehicleCoverage(
    val vehicle: Vehicle,
    val coveredZones: Set<String>
)
