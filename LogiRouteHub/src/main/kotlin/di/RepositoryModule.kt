package di

import data.repository.local.LocalPackageRepositoryImpl
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<WarehouseRepository> {
        LocalWarehouseRepositoryImpl(
            warehouseDataSource = get(),
            warehouseMapper = get()
        )
    }

    single<PackageRepository> {
        LocalPackageRepositoryImpl(
            packageDataSource = get(),
            packageMapper = get(),
            warehouseRepository = get()
        )
    }
}
