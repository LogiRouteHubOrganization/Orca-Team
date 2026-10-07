package domain.algorithm.greedy

import domain.model.Vehicle

class GreedyFleetDispatcher {

    /*
     * Greedy Set-Covering approach:
     * At each iteration, the algorithm selects the vehicle that covers
     * the largest number of currently uncovered target zones.
     *
     * The greedy heuristic runs in polynomial time because it repeatedly
     * scans the available vehicles instead of testing every possible
     * combination of vehicles. In the simplified case of N vehicles/zones,
     * this results in approximately O(N^2) work.
     *
     * A brute-force solution would examine possible subsets of vehicles,
     * resulting in exponential O(2^N) time.
     */
    fun dispatch(
        targetZones: Set<String>,
        vehicleCoverages: List<VehicleCoverage>
    ): DispatchResult {

        val uncoveredZones = targetZones.toMutableSet()
        val remainingVehicleCoverages = vehicleCoverages.toMutableList()
        val selectedVehicles = mutableListOf<Vehicle>()

        while (
            uncoveredZones.isNotEmpty() &&
            remainingVehicleCoverages.isNotEmpty()
        ) {

            val bestVehicleCoverage =
                remainingVehicleCoverages.maxByOrNull { coverage ->
                    coverage.coveredZones.count { zone ->
                        zone in uncoveredZones
                    }
                }
            val newlyCoveredZones = bestVehicleCoverage
                ?.coveredZones
                ?.filter { zone ->
                    zone in uncoveredZones
                }
                ?.toSet()
                ?: emptySet()

            if (bestVehicleCoverage == null || newlyCoveredZones.isEmpty()) {
                break
            }
            selectedVehicles.add(bestVehicleCoverage.vehicle)
            uncoveredZones.removeAll(newlyCoveredZones)
            remainingVehicleCoverages.remove(bestVehicleCoverage)
        }
        return DispatchResult(
            selectedVehicles = selectedVehicles,
            uncoveredZones = uncoveredZones
        )
    }
}
