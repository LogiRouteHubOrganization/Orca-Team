package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Route
import domain.model.request.EstimateDeliveryTimeRequest
import domain.model.request.RouteRiskLevel
import domain.usecase.EstimateDeliveryTimeUseCase
import domain.usecase.EstimateRouteRiskUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class EstimateDeliveryTimeUseCaseTest {

    @Test
    fun `should calculate delivery time with low risk`() {

        // Given
        val estimateRouteRiskUseCase = mockk<EstimateRouteRiskUseCase>()
        val route = mockk<Route>()

        every { route.distanceKm } returns 120.0
        every { route.typicalDelayMin } returns 10

        every {
            estimateRouteRiskUseCase(route)
        } returns RouteRiskLevel.LOW

        val request = EstimateDeliveryTimeRequest(
            routes = listOf(route),
            averageSpeedKmH = 60.0,
            minutesPerHour = 60.0,
            mediumRiskBufferMinutes = 20.0,
            highRiskBufferMinutes = 40.0,
            zeroTime = 0.0
        )

        val useCase = EstimateDeliveryTimeUseCase(
            estimateRouteRiskUseCase
        )

        // When
        val result = useCase(request)

        // Then
        assertThat(result).isEqualTo(130.0)

        verify {
            estimateRouteRiskUseCase(route)
        }
    }

    @Test
    fun `should calculate delivery time with high risk buffer`() {

        // Given
        val estimateRouteRiskUseCase = mockk<EstimateRouteRiskUseCase>()
        val route = mockk<Route>()

        every { route.distanceKm } returns 100.0
        every { route.typicalDelayMin } returns 10

        every {
            estimateRouteRiskUseCase(route)
        } returns RouteRiskLevel.HIGH

        val request = EstimateDeliveryTimeRequest(
            routes = listOf(route),
            averageSpeedKmH = 50.0,
            minutesPerHour = 60.0,
            mediumRiskBufferMinutes = 20.0,
            highRiskBufferMinutes = 30.0,
            zeroTime = 0.0
        )

        val useCase = EstimateDeliveryTimeUseCase(
            estimateRouteRiskUseCase
        )

        // When
        val result = useCase(request)

        // Then
        assertThat(result).isEqualTo(160.0)
    }

    @Test
    fun `should return zero when average speed is zero`() {

        // Given
        val estimateRouteRiskUseCase = mockk<EstimateRouteRiskUseCase>()

        val request = EstimateDeliveryTimeRequest(
            routes = emptyList(),
            averageSpeedKmH = 0.0,
            minutesPerHour = 60.0,
            mediumRiskBufferMinutes = 20.0,
            highRiskBufferMinutes = 30.0,
            zeroTime = 0.0
        )

        val useCase = EstimateDeliveryTimeUseCase(
            estimateRouteRiskUseCase
        )

        // When
        val result = useCase(request)

        // Then
        assertThat(result).isEqualTo(0.0)

        verify(exactly = 0) {
            estimateRouteRiskUseCase(any())
        }
    }
}
