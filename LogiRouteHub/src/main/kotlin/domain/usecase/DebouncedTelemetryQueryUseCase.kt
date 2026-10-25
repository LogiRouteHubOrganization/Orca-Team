package domain.usecase

import domain.model.Vehicle
import domain.repository.VehicleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

@OptIn(
    FlowPreview::class,
    ExperimentalCoroutinesApi::class
)
class DebouncedTelemetryQueryUseCase(
    private val vehicleRepository: VehicleRepository,
    private val searchDelayMillis: Long = 0L
) {

    fun execute(
        queries: Flow<String>
    ): Flow<Result<Vehicle?>> {

        return queries
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { vehicleId ->
                flow {
                    println("Search started: $vehicleId")

                    try {
                        if (searchDelayMillis > 0) {
                            kotlinx.coroutines.delay(searchDelayMillis)
                        }

                        val result = vehicleRepository.findById(vehicleId)

                        emit(result)

                    } catch (e: CancellationException) {
                        println("Search cancelled: $vehicleId")
                        throw e
                    }
                }
            }
    }
}