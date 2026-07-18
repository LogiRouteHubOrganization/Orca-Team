package model

data class LogisticsPackage(
    val id: String,
    val weight: String,
    val destinationHubId: String,
    val priority: String
)