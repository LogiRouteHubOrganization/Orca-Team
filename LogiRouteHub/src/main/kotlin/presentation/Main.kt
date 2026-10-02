package presentation

import di.networkModule
import di.repositoryModule
import di.validatorModule
import di.useCaseModule
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main() = runBlocking {

    startKoin {
        modules(
            networkModule,
            repositoryModule,
            validatorModule,
            useCaseModule
        )
    }

    println("Application started successfully with Koin")
}
