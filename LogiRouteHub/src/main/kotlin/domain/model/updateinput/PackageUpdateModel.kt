package domain.model.updateinput

data class PackageUpdateModel(
    val weightByKg: Double? = null,
    val volumeM3: Double? = null,
    val priority: String? = null,
    val originWarehouseId: String? = null,
    val destinationWarehouseId: String? = null
)
