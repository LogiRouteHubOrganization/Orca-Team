package domain.algorithm.geohash

private const val GEOHASH_ALPHABET = "0123456789bcdefghjkmnpqrstuvwxyz"

data class BoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    val centerLat: Double get() = (minLat + maxLat) / 2.0
    val centerLon: Double get() = (minLon + maxLon) / 2.0
    val latHeight: Double get() = maxLat - minLat
    val lonWidth: Double get() = maxLon - minLon
}
data class NeighboringBoxes(
    val north: String,
    val south: String,
    val east: String,
    val west: String,
    val northEast: String,
    val northWest: String,
    val southEast: String,
    val southWest: String
) {
    fun toList(): List<String> = listOf(
        north, south, east, west,
        northEast, northWest, southEast, southWest
    )
}
fun decodeGeoHash(geohash: String): BoundingBox {
    require(geohash.isNotEmpty()) { "Geohash cannot be empty" }
    var minLat = -90.0
    var maxLat = 90.0
    var minLon = -180.0
    var maxLon = 180.0
    var isLongitude = true

    for (char in geohash) {
        val charValue = GEOHASH_ALPHABET.indexOf(char)
        require(charValue != -1) { "Invalid Geohash character: $char" }

        for (bitIndex in 4 downTo 0) {
            val bit = (charValue shr bitIndex) and 1
            if (isLongitude) {
                val mid = (minLon + maxLon) / 2.0
                if (bit == 1) minLon = mid else maxLon = mid
            } else {
                val mid = (minLat + maxLat) / 2.0
                if (bit == 1) minLat = mid else maxLat = mid
            }
            isLongitude = !isLongitude
        }
    }

    return BoundingBox(minLat, maxLat, minLon, maxLon)
}


fun getNeighbors(geohash: String): NeighboringBoxes {
    val box = decodeGeoHash(geohash)
    val precision = geohash.length
    val latDelta = box.latHeight
    val lonDelta = box.lonWidth
    val centerLat = box.centerLat
    val centerLon = box.centerLon
    fun safeEncode(lat: Double, lon: Double): String {
        val safeLat = lat.coerceIn(-90.0, 90.0)
        var safeLon = lon
        if (safeLon > 180.0) safeLon -= 360.0
        if (safeLon < -180.0) safeLon += 360.0
        return encodeGeoHash(
            Longitude = safeLon,
            Latitude = safeLat,
            Precision = precision
        )
    }
    return NeighboringBoxes(
        north = safeEncode(centerLat + latDelta, centerLon),
        south = safeEncode(centerLat - latDelta, centerLon),
        east = safeEncode(centerLat, centerLon + lonDelta),
        west = safeEncode(centerLat, centerLon - lonDelta),
        northEast = safeEncode(centerLat + latDelta, centerLon + lonDelta),
        northWest = safeEncode(centerLat + latDelta, centerLon - lonDelta),
        southEast = safeEncode(centerLat - latDelta, centerLon + lonDelta),
        southWest = safeEncode(centerLat - latDelta, centerLon - lonDelta)
    )
}