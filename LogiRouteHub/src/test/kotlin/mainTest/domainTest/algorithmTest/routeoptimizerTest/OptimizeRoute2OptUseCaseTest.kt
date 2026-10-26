package mainTest.domainTest.algorithmTest.routeoptimizerTest

import domain.model.Warehouse
import domain.usecase.OptimizeRoute2OptUseCase
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OptimizeRoute2OptUseCaseTest {

    @Test
    fun test_optimization_reduces_distance() {
        val useCase = OptimizeRoute2OptUseCase()

        val origin = Warehouse(
            id = "WH-0",
            name = "Main Hub",
            regionalZone = "ZONE-A",
            latitude = 31.5,
            longitude = 34.45
        )

        val stops = (1..15).map { i ->
            Warehouse(
                id = "WH-$i",
                name = "Stop $i",
                regionalZone = "ZONE-A",
                latitude = 31.5 + (i * 0.01),
                longitude = 34.45 + (if (i % 2 == 0) 0.02 else -0.02)
            )
        }

        val (route, metrics) = useCase(origin, stops)

        assertEquals(origin.id, route.first().id)
        assertEquals(origin.id, route.last().id)
        assertEquals(17, route.size)

        assertTrue(metrics["initialDistance"]!! >= metrics["finalDistance"]!!)
        assertTrue(metrics.containsKey("distanceSaved"))
        assertTrue(metrics.containsKey("savingsPercentage"))
    }
}





