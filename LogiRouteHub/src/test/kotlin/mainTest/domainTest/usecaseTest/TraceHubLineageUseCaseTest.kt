package mainTest.domainTest.usecaseTest



import com.google.common.truth.Truth.assertThat
import domain.algorithm.tree.hierarchy.HubLineageTracer
import domain.algorithm.tree.hierarchy.HubTreeWarehouseNode
import domain.usecase.TraceHubLineageUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TraceHubLineageUseCaseTest {

    @Test
    fun `should return hub lineage for local depot`() {

        // Given
        val hubLineageTracer = mockk<HubLineageTracer>()

        val localDepotNode = mockk<HubTreeWarehouseNode>()
        val parentNode = mockk<HubTreeWarehouseNode>()
        val rootNode = mockk<HubTreeWarehouseNode>()

        val expectedLineage = listOf(
            localDepotNode,
            parentNode,
            rootNode
        )

        every {
            hubLineageTracer.traceLineage(localDepotNode)
        } returns expectedLineage

        val useCase = TraceHubLineageUseCase(
            hubLineageTracer
        )

        // When
        val result = useCase(localDepotNode)

        // Then
        assertThat(result).isEqualTo(expectedLineage)

        verify {
            hubLineageTracer.traceLineage(localDepotNode)
        }
    }
}

