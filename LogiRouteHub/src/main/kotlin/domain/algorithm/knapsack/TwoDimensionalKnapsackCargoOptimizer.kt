package domain.algorithm.knapsack

import domain.model.Package
import domain.model.request.PackagePriorityLevels
import kotlin.math.roundToInt

class TwoDimensionalKnapsackCargoOptimizer {

    private val priorityLevels = PackagePriorityLevels()

    companion object {
        private const val SCALE_FACTOR = 100
        private const val ZERO_CAPACITY = 0.0
    }

    fun optimize(
        packages: List<Package>,
        capacityKg: Double,
        capacityVolumeM3: Double
    ): List<Package> {

        if (packages.isEmpty() || capacityKg <= ZERO_CAPACITY || capacityVolumeM3 <= ZERO_CAPACITY) {
            return emptyList()
        }

        val scaledPackages = packages.map { logisticsPackage ->
            ScaledPackage(
                packageData = logisticsPackage,
                weight = (logisticsPackage.weightByKg * SCALE_FACTOR).roundToInt(),
                volume = ((logisticsPackage.volumeM3 ?: ZERO_CAPACITY) * SCALE_FACTOR).roundToInt()
            )
        }

        val capacity = CargoCapacity(
            weight = (capacityKg * SCALE_FACTOR).roundToInt(),
            volume = (capacityVolumeM3 * SCALE_FACTOR).roundToInt()
        )

        return findOptimalPackages(
            packages = scaledPackages,
            capacity = capacity
        )
    }

    private fun findOptimalPackages(
        packages: List<ScaledPackage>,
        capacity: CargoCapacity
    ): List<Package> {

        var states = mutableMapOf(
            CapacityState(0, 0) to SelectionState(0, emptyList())
        )
        packages.indices.forEach { packageIndex ->
            val currentPackage = packages[packageIndex]
            val updatedStates = states.toMutableMap()

            states.forEach { (currentCapacity, currentSelection) -> val newCapacity = CapacityState(
                    weight = currentCapacity.weight + currentPackage.weight,
                    volume = currentCapacity.volume + currentPackage.volume
                )

                if (fitsCapacity(newCapacity, capacity)) {
                    val newSelection = SelectionState(
                        priority = currentSelection.priority + getPriority(currentPackage.packageData.priority),
                        packageIndices = currentSelection.packageIndices + packageIndex
                    )
                    updateState(
                        states = updatedStates,
                        capacity = newCapacity,
                        selection = newSelection
                    )
                }
            }
            states = updatedStates
        }
        return getSelectedPackages(states = states, packages = packages)
    }

    private fun fitsCapacity(
        current: CapacityState,
        maximum: CargoCapacity
    ): Boolean {
        return current.weight <= maximum.weight && current.volume <= maximum.volume
    }

    private fun updateState(
        states: MutableMap<CapacityState, SelectionState>,
        capacity: CapacityState,
        selection: SelectionState
    ) {
        val existingSelection = states[capacity]

        if (
            existingSelection == null || selection.priority > existingSelection.priority) {
            states[capacity] = selection
        }
    }
    private fun getSelectedPackages(
        states: Map<CapacityState, SelectionState>,
        packages: List<ScaledPackage>
    ): List<Package> {

        val bestSelection = states.values.maxByOrNull { it.priority } ?: return emptyList()

        return bestSelection.packageIndices.map { index ->
            packages[index].packageData
        }
    }

    private fun getPriority(priority: String): Int {
        return when (priority.uppercase()) {
            "URGENT" -> priorityLevels.urgent
            "STANDARD" -> priorityLevels.standard
            "LOW" -> priorityLevels.low
            else -> priorityLevels.unknown
        }
    }

    private data class ScaledPackage(
        val packageData: Package,
        val weight: Int,
        val volume: Int
    )

    private data class CargoCapacity(
        val weight: Int,
        val volume: Int
    )

    private data class CapacityState(
        val weight: Int,
        val volume: Int
    )

    private data class SelectionState(
        val priority: Int,
        val packageIndices: List<Int>
    )
}
