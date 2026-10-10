package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Package
import domain.model.Route
import domain.model.Vehicle
import domain.model.request.CalculatePricingRequest
import domain.pricing.DispatchStrategy
import domain.pricing.decorator.PackageComponent
import domain.pricing.decorator.PackageDecoratorComposer
import domain.usecase.CalculatePricingUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CalculatePricingUseCaseTest {

    private val packageDecoratorComposer =
        mockk<PackageDecoratorComposer>()

    private val useCase = CalculatePricingUseCase(
        packageDecoratorComposer = packageDecoratorComposer
    )

    @Test
    fun `returns calculated price from decorated package`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val selectedRoute = mockk<Route>()
        val assignedVehicle = mockk<Vehicle>()
        val dispatchStrategy = mockk<DispatchStrategy>()
        val decoratedPackage = mockk<PackageComponent>()

        val expectedPrice = 250.0

        val request = createRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )

        every {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = true,
                requiresColdChain = false,
                needsExpressInsurance = true
            )
        } returns decoratedPackage

        every {
            decoratedPackage.calculateTransitRate(
                selectedRoute = selectedRoute,
                assignedVehicle = assignedVehicle,
                dispatchStrategy = dispatchStrategy
            )
        } returns expectedPrice

        // When
        val result = useCase(request)

        // Then
        assertThat(result.getOrNull())
            .isEqualTo(expectedPrice)
    }

    @Test
    fun `passes decorator options from request to composer`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val selectedRoute = mockk<Route>()
        val assignedVehicle = mockk<Vehicle>()
        val dispatchStrategy = mockk<DispatchStrategy>()
        val decoratedPackage = mockk<PackageComponent>()

        val request = createRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )

        every {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = true,
                requiresColdChain = false,
                needsExpressInsurance = true
            )
        } returns decoratedPackage

        every {
            decoratedPackage.calculateTransitRate(
                selectedRoute = selectedRoute,
                assignedVehicle = assignedVehicle,
                dispatchStrategy = dispatchStrategy
            )
        } returns 250.0

        // When
        useCase(request)

        // Then
        verify(exactly = 1) {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = true,
                requiresColdChain = false,
                needsExpressInsurance = true
            )
        }
    }

    @Test
    fun `returns failure when decorator composition fails`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val selectedRoute = mockk<Route>()
        val assignedVehicle = mockk<Vehicle>()
        val dispatchStrategy = mockk<DispatchStrategy>()

        val exception = RuntimeException(
            "Decorator composition failed"
        )

        val request = createRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )

        every {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = true,
                requiresColdChain = false,
                needsExpressInsurance = true
            )
        } throws exception

        // When
        val result = useCase(request)

        // Then
        assertThat(result.exceptionOrNull())
            .isEqualTo(exception)
    }

    @Test
    fun `returns failure when price calculation fails`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val selectedRoute = mockk<Route>()
        val assignedVehicle = mockk<Vehicle>()
        val dispatchStrategy = mockk<DispatchStrategy>()
        val decoratedPackage = mockk<PackageComponent>()

        val exception = RuntimeException(
            "Price calculation failed"
        )

        val request = createRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy
        )

        every {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = true,
                requiresColdChain = false,
                needsExpressInsurance = true
            )
        } returns decoratedPackage

        every {
            decoratedPackage.calculateTransitRate(
                selectedRoute = selectedRoute,
                assignedVehicle = assignedVehicle,
                dispatchStrategy = dispatchStrategy
            )
        } throws exception

        // When
        val result = useCase(request)

        // Then
        assertThat(result.exceptionOrNull())
            .isEqualTo(exception)
    }

    private fun createRequest(
        logisticsPackage: Package,
        selectedRoute: Route,
        assignedVehicle: Vehicle,
        dispatchStrategy: DispatchStrategy
    ): CalculatePricingRequest {

        return CalculatePricingRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy,
            isFragile = true,
            requiresColdChain = false,
            needsExpressInsurance = true
        )
    }
}

