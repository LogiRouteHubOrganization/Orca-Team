package model

data class Route(
    val routeId: String,
    val originHubId: String,
    val destinationHubId: String,
    val distanceKm: String,
    val typicalDelayMin: String
)