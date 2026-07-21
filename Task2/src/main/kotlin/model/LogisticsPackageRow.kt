package model


data class LogisticsPackageRow(
    val id: String,
    val packageWeight: Double,
    val destinationHubId: String,
    val priority: String
)