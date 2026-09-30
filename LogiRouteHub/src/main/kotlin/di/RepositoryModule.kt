package di

import data.repository.PackageRepositoryImpl
import data.repository.RouteRepositoryImpl
import data.repository.VehicleRepositoryImpl
import data.repository.WarehouseRepositoryImpl
import domain.repository.PackageRepository
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository
import org.koin.dsl.module

val repositoryModule = module {

    single<WarehouseRepository> {
        WarehouseRepositoryImpl(
            warehouseDataSource = get(),
            warehouseRemoteDataSource = get(),
            warehouseMapper = get()
        )
    }

    single<PackageRepository> {
        PackageRepositoryImpl(
            packageDataSource = get(),
            packageRemoteDataSource = get(),
            packageMapper = get(),
            warehouseRepository = get()
        )
    }

    single<VehicleRepository> {
        VehicleRepositoryImpl(
            vehicleDataSource = get(),
            vehicleRemoteDataSource = get(),
            vehicleMapper = get(),
            warehouseRepository = get()
        )
    }

    single<RouteRepository> {
        RouteRepositoryImpl(
            routeDataSource = get(),
            routeRemoteDataSource = get(),
            routeMapper = get(),
            warehouseRepository = get()
        )
    }
}
