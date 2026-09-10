package domain.model.request

data class FleetRebalancingConfig(
    val highLoadFactorThreshold: Double,
    val lowLoadFactorThreshold: Double,
    val maximumVehiclesToRebalance: Int
)
