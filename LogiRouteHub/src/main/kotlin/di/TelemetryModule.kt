
package di

import data.telemetry.SimulatedTelemetryStreamingService
import domain.service.TelemetryStreamingService
import org.koin.dsl.module

val telemetryModule = module {
    single<TelemetryStreamingService> {
        SimulatedTelemetryStreamingService()
    }
}
