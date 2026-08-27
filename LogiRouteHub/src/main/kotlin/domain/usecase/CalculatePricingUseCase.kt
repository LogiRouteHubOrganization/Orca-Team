package domain.usecase

import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.pricing.DispatchStrategy
import domain.pricing.decorator.PackageDecoratorComposer

class CalculatePricingUseCase(
    private val packageDecoratorComposer: PackageDecoratorComposer
) {
    operator fun invoke(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy,
        isFragile: Boolean,
        requiresColdChain: Boolean,
        needsExpressInsurance: Boolean
    ): Double {
        val decoratedPackage = packageDecoratorComposer.applyOptionalDecorators(
            packageComponent = logisticsPackage,
            isFragile = isFragile,
            requiresColdChain = requiresColdChain,
            needsExpressInsurance = needsExpressInsurance
        )

        return decoratedPackage.calculateTransitRate(
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )
    }
}

