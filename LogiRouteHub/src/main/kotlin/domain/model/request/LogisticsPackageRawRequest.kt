package domain.model.request

data class LogisticsPackageRawRequest(
    val packageId: String,
    val packageWeightByKg: Double,
    val originHubId: String,
    val destinationHubId: String,
    val packagePriority: String
)
