package domain.exception

sealed class ResourceNotFoundException(
    message: String
) : DomainException(message) {

    class RouteNotFound(routeId: String) :
        ResourceNotFoundException("Route with ID '$routeId' was not found")

    class VehicleNotFound(vehicleId: String) :
        ResourceNotFoundException("Vehicle with ID '$vehicleId' was not found")

    class WarehouseNotFound(warehouseId: String) :
        ResourceNotFoundException("Warehouse with ID '$warehouseId' was not found")

    class PackageNotFound(packageId: String) :
        ResourceNotFoundException("Package with ID '$packageId' was not found")

    class Default(resourceType: String = "Resource", resourceId: String = "") :
        ResourceNotFoundException(
            if (resourceId.isNotBlank()) "$resourceType with ID '$resourceId' was not found"
            else "$resourceType was not found"
        )
}
