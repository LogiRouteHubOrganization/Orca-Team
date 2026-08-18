package dataholder

data class LogisticsPackageRaw(
    val packageId: String,
    val packageWeightByKg: Double,
    val originHubId: String,
    val destinationHubId: String,
    val packagePriority: String
)
