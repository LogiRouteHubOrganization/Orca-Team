package domain.pricing.decorator

class PackageDecoratorComposer {

    fun applyOptionalDecorators(
        packageComponent: PackageComponent,
        isFragile: Boolean,
        requiresColdChain: Boolean,
        needsExpressInsurance: Boolean
    ): PackageComponent {

        var decoratedPackage = packageComponent

        if (isFragile) {
            decoratedPackage =
                FragileHandlingDecorator(decoratedPackage)
        }

        if (requiresColdChain) {
            decoratedPackage =
                ColdChainDecorator(decoratedPackage)
        }

        if (needsExpressInsurance) {
            decoratedPackage =
                ExpressInsuranceDecorator(decoratedPackage)
        }

        return decoratedPackage
    }
}