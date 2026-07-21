package model


data class LogisticsPackageRow(
    val id: String,
    val packageWeight: String,
    val destinationHubId: String,
    val priority: String
)