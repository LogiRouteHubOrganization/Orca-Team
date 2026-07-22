package dataHolder


data class LogisticsPackageRaw(
    val packageId: String,
    val packageWeightByKg: Double,
    val destinationHubId: String,
    val packagePriority: String
)