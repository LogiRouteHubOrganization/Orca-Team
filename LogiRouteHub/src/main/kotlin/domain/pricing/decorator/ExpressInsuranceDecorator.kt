package domain.pricing.decorator

import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy

private const val EXPRESS_INSURANCE_FEE = 50.0

class ExpressInsuranceDecorator(
    wrappedComponent: PackageComponent
) : PackageDecorator(wrappedComponent) {

    override fun calculateTransitRate(
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy
    ): Double {

        val baseTransitRate =
            wrappedComponent.calculateTransitRate(
                selectedRoute = selectedRoute,
                assignedVehicle = assignedVehicle,
                dispatchStrategy = dispatchStrategy
            )

        return baseTransitRate + EXPRESS_INSURANCE_FEE
    }
}