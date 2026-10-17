package di

import dispatcher.AppDispatchers
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module

val dispatcherModule = module {
    single {
        AppDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default
        )
    }
}
