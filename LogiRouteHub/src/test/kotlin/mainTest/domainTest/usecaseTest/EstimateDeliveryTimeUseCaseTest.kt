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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EstimateDeliveryTimeUseCaseTest {

    private val estimateRouteRiskUseCase = mockk<EstimateRouteRiskUseCase>()
    private lateinit var useCase: EstimateDeliveryTimeUseCase

    @BeforeEach
    fun setUp() {
        useCase = EstimateDeliveryTimeUseCase(estimateRouteRiskUseCase)
    }

    @Test
    fun `should calculate correct delivery time for low risk route`() {
        val route = stubRoute(distanceKm = 120.0, typicalDelayMin = 10, riskLevel = RouteRiskLevel.LOW)
        val request = createRequest(routes = listOf(route), averageSpeedKmH = 60.0)

        val result = useCase(request)

        assertThat(result).isEqualTo(130.0)
    }

    @Test
    fun `should invoke estimate route risk use case for low risk route`() {
        val route = stubRoute(distanceKm = 120.0, typicalDelayMin = 10, riskLevel = RouteRiskLevel.LOW)
        val request = createRequest(routes = listOf(route), averageSpeedKmH = 60.0)

        useCase(request)

        verify(exactly = 1) { estimateRouteRiskUseCase(route) }
    }


    @Test
    fun `should calculate correct delivery time including high risk buffer`() {
        val route = stubRoute(distanceKm = 100.0, typicalDelayMin = 10, riskLevel = RouteRiskLevel.HIGH)
        val request = createRequest(
            routes = listOf(route),
            averageSpeedKmH = 50.0,
            highRiskBufferMinutes = 30.0
        )

        val result = useCase(request)

        assertThat(result).isEqualTo(160.0)
    }


    @Test
    fun `should return zero time when average speed is zero`() {
        val request = createRequest(routes = emptyList(), averageSpeedKmH = 0.0)

        val result = useCase(request)

        assertThat(result).isEqualTo(0.0)
    }

    @Test
    fun `should skip route risk estimation when average speed is zero`() {
        val request = createRequest(routes = emptyList(), averageSpeedKmH = 0.0)

        useCase(request)

        verify(exactly = 0) { estimateRouteRiskUseCase(any()) }
    }


    private fun stubRoute(
        distanceKm: Double,
        typicalDelayMin: Int,
        riskLevel: RouteRiskLevel
    ): Route {
        val route = mockk<Route>()
        every { route.distanceKm } returns distanceKm
        every { route.typicalDelayMin } returns typicalDelayMin
        every { estimateRouteRiskUseCase(route) } returns riskLevel
        return route
    }

    private fun createRequest(
        routes: List<Route>,
        averageSpeedKmH: Double,
        minutesPerHour: Double = 60.0,
        mediumRiskBufferMinutes: Double = 20.0,
        highRiskBufferMinutes: Double = 40.0,
        zeroTime: Double = 0.0
    ): EstimateDeliveryTimeRequest {
        return EstimateDeliveryTimeRequest(
            routes = routes,
            averageSpeedKmH = averageSpeedKmH,
            minutesPerHour = minutesPerHour,
            mediumRiskBufferMinutes = mediumRiskBufferMinutes,
            highRiskBufferMinutes = highRiskBufferMinutes,
            zeroTime = zeroTime
        )
    }
}