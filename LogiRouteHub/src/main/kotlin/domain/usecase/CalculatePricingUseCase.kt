package domain.usecase

import domain.model.request.CalculatePricingRequest
import domain.pricing.decorator.PackageDecoratorComposer

class CalculatePricingUseCase(
    private val packageDecoratorComposer: PackageDecoratorComposer
) {

    operator fun invoke(
        request: CalculatePricingRequest
    ): Result<Double> = runCatching {

        val decoratedPackage = packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = request.logisticsPackage,
                isFragile = request.isFragile,
                requiresColdChain = request.requiresColdChain,
                needsExpressInsurance = request.needsExpressInsurance
            )

        decoratedPackage.calculateTransitRate(
            selectedRoute = request.selectedRoute,
            assignedVehicle = request.assignedVehicle,
            dispatchStrategy = request.dispatchStrategy
        )
    }
}
