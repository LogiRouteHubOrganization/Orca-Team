package mainTest.domainTest.usecaseTest

import com.google.common.truth.Truth.assertThat
import domain.model.Route
import domain.model.request.RouteRiskConfig
import domain.model.request.RouteRiskLevel
import domain.usecase.EstimateRouteRiskUseCase
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class EstimateRouteRiskUseCaseTest {

    private val riskConfig = RouteRiskConfig(
        highDistanceThresholdKm = 100.0,
        highDelayThresholdMin = 60,
        mediumRiskScoreThreshold = 1.0,
        highRiskScoreThreshold = 2.0
    )

    private val useCase = EstimateRouteRiskUseCase(riskConfig)

    @Test
    fun `returns LOW when total risk score is below medium threshold`() {
        // Given
        val route = mockk<Route> {
            every { distanceKm } returns 40.0
            every { typicalDelayMin } returns 18
        }

        // When
        val result = useCase(route)

        // Then
        assertThat(result).isEqualTo(RouteRiskLevel.LOW)
    }

    @Test
    fun `returns MEDIUM when total risk score is between medium and high thresholds`() {
        // Given
        val route = mockk<Route> {
            every { distanceKm } returns 60.0
            every { typicalDelayMin } returns 30
        }

        // distance score = 0.6
        // delay score = 0.5
        // total = 1.1

        // When
        val result = useCase(route)

        // Then
        assertThat(result).isEqualTo(RouteRiskLevel.MEDIUM)
    }

    @Test
    fun `returns HIGH when total risk score is above high threshold`() {
        // Given
        val route = mockk<Route> {
            every { distanceKm } returns 120.0
            every { typicalDelayMin } returns 60
        }

        // distance score = 1.2
        // delay score = 1.0
        // total = 2.2

        // When
        val result = useCase(route)

        // Then
        assertThat(result).isEqualTo(RouteRiskLevel.HIGH)
    }

    @Test
    fun `returns MEDIUM when total risk score equals medium threshold`() {
        // Given
        val route = mockk<Route> {
            every { distanceKm } returns 60.0
            every { typicalDelayMin } returns 24
        }

        // distance score = 0.6
        // delay score = 0.4
        // total = 1.0

        // When
        val result = useCase(route)

        // Then
        assertThat(result).isEqualTo(RouteRiskLevel.MEDIUM)
    }

    @Test
    fun `returns HIGH when total risk score equals high threshold`() {
        // Given
        val route = mockk<Route> {
            every { distanceKm } returns 100.0
            every { typicalDelayMin } returns 60
        }

        // distance score = 1.0
        // delay score = 1.0
        // total = 2.0

        // When
        val result = useCase(route)

        // Then
        assertThat(result).isEqualTo(RouteRiskLevel.HIGH)
    }
}
