package domain.usecase

import domain.pricing.decorator.PackageDecoratorComposer
import domain.model.request.CalculatePricingRequest

class CalculatePricingUseCase(
    private val packageDecoratorComposer: PackageDecoratorComposer
) {
    operator fun invoke(request: CalculatePricingRequest): Double {

        val decoratedPackage = packageDecoratorComposer.applyOptionalDecorators(
            packageComponent = request.logisticsPackage,
            isFragile = request.isFragile,
            requiresColdChain = request.requiresColdChain,
            needsExpressInsurance = request.needsExpressInsurance
        )

        return decoratedPackage.calculateTransitRate(
            selectedRoute = request.selectedRoute,
            assignedVehicle = request.assignedVehicle,
            dispatchStrategy = request.dispatchStrategy
        )
    }
}
