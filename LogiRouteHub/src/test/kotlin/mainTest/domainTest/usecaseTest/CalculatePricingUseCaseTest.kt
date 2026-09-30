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

    private val packageDecoratorComposer = mockk<PackageDecoratorComposer>()

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

        val request = CalculatePricingRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy,
            isFragile = true,
            requiresColdChain = false,
            needsExpressInsurance = true
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
        assertThat(result).isEqualTo(expectedPrice)
        verify(exactly = 1) {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = true,
                requiresColdChain = false,
                needsExpressInsurance = true
            )
        }
        verify(exactly = 1) {
            decoratedPackage.calculateTransitRate(
                selectedRoute = selectedRoute,
                assignedVehicle = assignedVehicle,
                dispatchStrategy = dispatchStrategy
            )
        }
    }

    @Test
    fun `passes disabled optional decorators correctly`() {
        // Given
        val logisticsPackage = mockk<Package>()
        val selectedRoute = mockk<Route>()
        val assignedVehicle = mockk<Vehicle>()
        val dispatchStrategy = mockk<DispatchStrategy>()
        val decoratedPackage = mockk<PackageComponent>()

        val expectedPrice = 100.0

        val request = CalculatePricingRequest(
            logisticsPackage = logisticsPackage,
            selectedRoute = selectedRoute,
            assignedVehicle = assignedVehicle,
            dispatchStrategy = dispatchStrategy,
            isFragile = false,
            requiresColdChain = false,
            needsExpressInsurance = false
        )

        every {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = false,
                requiresColdChain = false,
                needsExpressInsurance = false
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
        assertThat(result).isEqualTo(expectedPrice)

        verify(exactly = 1) {
            packageDecoratorComposer.applyOptionalDecorators(
                packageComponent = logisticsPackage,
                isFragile = false,
                requiresColdChain = false,
                needsExpressInsurance = false
            )
        }

        verify(exactly = 1) {
            decoratedPackage.calculateTransitRate(
                selectedRoute = selectedRoute,
                assignedVehicle = assignedVehicle,
                dispatchStrategy = dispatchStrategy
            )
        }
    }
}
