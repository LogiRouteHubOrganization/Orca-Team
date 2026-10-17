package domain.model.request

class DelayTrackingState {
    val trackingByVehicle = mutableMapOf<String, VehicleTracking>()
}

