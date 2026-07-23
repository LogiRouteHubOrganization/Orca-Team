package dataHolder
data class RoutesRaw(
    val routeId: String,
    val originHubId: String,
    val destinationHubId: String,
    val distanceKm: String,
    val typicalDelayMin: String)