package domain.algorithm.geohash

import domain.model.Warehouse
import domain.algorithm.geoHash.encodeGeoHash
import domain.algorithm.geoHash.getNeighbors

class WarehouseSpatialIndex(
    private val precision: Int = 5
) {
    private val index: MutableMap<String, MutableList<Warehouse>> = HashMap()

    fun addWarehouse(warehouse: Warehouse) {
        val prefix = encodeGeoHash(
            Longitude = warehouse.longitude,
            Latitude = warehouse.latitude,
            Precision = precision
        )
        index.getOrPut(prefix) { mutableListOf() }.add(warehouse)
    }

    fun buildIndex(warehouses: List<Warehouse>) {
        warehouses.forEach { addWarehouse(it) }
    }

    fun findNearbyWarehouses(latitude: Double, longitude: Double): List<Warehouse> {
        val currentHash = encodeGeoHash(
            Longitude = longitude,
            Latitude = latitude,
            Precision = precision
        )

        val neighbors = getNeighbors(currentHash)
        val searchPrefixes = listOf(currentHash) + neighbors.toList()

        val results = mutableListOf<Warehouse>()
        for (prefix in searchPrefixes) {
            index[prefix]?.let { warehousesInBox ->
                results.addAll(warehousesInBox)
            }
        }

        return results
    }
}//