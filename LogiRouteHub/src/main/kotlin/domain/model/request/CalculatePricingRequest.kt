package domain.model.request

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy

data class CalculatePricingRequest(
    val logisticsPackage: Package,
    val selectedRoute: Route,
    val assignedVehicle: Vehicle,
    val dispatchStrategy: DispatchStrategy,
    val isFragile: Boolean,
    val requiresColdChain: Boolean,
    val needsExpressInsurance: Boolean
)
