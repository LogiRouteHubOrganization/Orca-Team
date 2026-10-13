package diTest

import com.google.common.truth.Truth.assertThat
import domain.repository.WarehouseRepository
import domain.usecase.FindOptimalPathUseCase
import di.useCaseModule
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject

class KoinDependencyInjectionTest : KoinTest {

    private lateinit var warehouseRepository: WarehouseRepository

    private val findOptimalPathUseCase: FindOptimalPathUseCase by inject()

    @BeforeEach
    fun setUp() {
        warehouseRepository = mockk()

        startKoin {
            modules(
                useCaseModule,
                module {
                    single<WarehouseRepository> {
                        warehouseRepository
                    }
                }
            )
        }
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `Koin should resolve FindOptimalPathUseCase with injected WarehouseRepository`() {
        assertThat(findOptimalPathUseCase).isNotNull()
    }
}
