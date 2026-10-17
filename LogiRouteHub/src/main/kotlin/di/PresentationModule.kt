package di

import core.dispatcher.AppDispatchers
import org.koin.dsl.module
import presentation.cli.ClassificationCommand
import presentation.cli.ConsoleIO
import presentation.cli.FleetTrackingCommand
import presentation.cli.InteractiveCli
import presentation.cli.RouteCommand
import presentation.cli.SystemConsoleIO

val presentationModule = module {

    single<ConsoleIO> {
        SystemConsoleIO(
            ioDispatcher = get<AppDispatchers>().io
        )
    }

    single {
        RouteCommand(
            findOptimalPathUseCase = get(),
            console = get(),
            dispatchers = get()
        )
    }

    single {
        ClassificationCommand(
            classifyPackageSectorUseCase = get(),
            console = get(),
            dispatchers = get(),
            latitudeValidator = get(),
            longitudeValidator = get()
        )
    }

    single {
        FleetTrackingCommand(
            telemetryService = get(),
            trackActiveFleetUseCase = get(),
            console = get()
        )
    }

    single {
        InteractiveCli(
            routeCommand = get(),
            classificationCommand = get(),
            fleetTrackingCommand = get(),
            console = get()
        )
    }
}