package model


data class LogisticsPackageRow(
    val id: String,
    val weight: String,
    val destinationHubId: String,
    val priority: String
)