package di

import data.remote.SupabaseClientProvider
import data.remote.retry.RetryableRemoteDatasource
import org.koin.dsl.module

val networkModule = module {

    single {
        SupabaseClientProvider.client
    }

    single {
        RetryableRemoteDatasource()
    }
}