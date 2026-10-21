
package di

import data.datasource.local.LocalPackageDataSource
import data.datasource.local.LocalRouteDataSource
import data.datasource.local.LocalVehicleDataSource
import data.datasource.local.LocalWarehouseDataSource
import data.datasource.local.csv.CsvFileLoader
import data.datasource.local.csv.CsvPackageDataSourceImpl
import data.datasource.local.csv.CsvRouteDataSourceImpl
import data.datasource.local.csv.CsvVehicleDataSourceImpl
import data.datasource.local.csv.CsvWarehouseDataSourceImpl
import data.mapper.PackageMapper
import data.mapper.RouteMapper
import data.mapper.VehicleMapper
import data.mapper.WarehouseMapper
import data.parser.FleetParser
import data.parser.PackageParser
import data.parser.RouteParser
import data.parser.WarehouseParser
import data.datasource.remote.RemotePackageDataSource
import data.datasource.remote.RemoteRouteDataSource
import data.datasource.remote.RemoteVehicleDataSource
import data.datasource.remote.RemoteWarehouseDataSource
import data.datasource.remote.supabase.SupabasePackageDataSourceImpl
import data.datasource.remote.supabase.SupabaseRouteDataSourceImpl
import data.datasource.remote.supabase.SupabaseVehicleDataSourceImpl
import data.datasource.remote.supabase.SupabaseWarehouseDataSourceImpl
import data.repository.PackageRepositoryImpl
import data.repository.RouteRepositoryImpl
import data.repository.VehicleRepositoryImpl
import data.repository.WarehouseRepositoryImpl
import data.validation.LocalDataSourceValidator
import domain.repository.PackageRepository
import domain.repository.RouteRepository
import domain.repository.VehicleRepository
import domain.repository.WarehouseRepository

import org.koin.dsl.module

import java.io.File

private const val PACKAGES_FILE_PATH =
    "src/main/resources/packages.csv"

private const val WAREHOUSES_FILE_PATH =
    "src/main/resources/warehouses.csv"

private const val ROUTES_FILE_PATH =
    "src/main/resources/routes.csv"

private const val FLEET_FILE_PATH =
    "src/main/resources/fleet.csv"

val repositoryModule = module {


    single {
        LocalDataSourceValidator()
    }


    single {
        CsvFileLoader(
            validator = get()
        )
    }


    single {
        PackageParser()
    }

    single {
        WarehouseParser()
    }

    single {
        RouteParser()
    }

    single {
        FleetParser()
    }

    single {
        PackageMapper()
    }

    single {
        WarehouseMapper()
    }

    single {
        RouteMapper()
    }

    single {
        VehicleMapper()
    }

    // Local Data Sources
    single<LocalWarehouseDataSource> {
        CsvWarehouseDataSourceImpl(
            warehouseFile = File(WAREHOUSES_FILE_PATH),
            warehouseParser = get(),
            csvFileLoader = get(),
            appDispatchers = get()
        )
    }

    single<LocalPackageDataSource> {
        CsvPackageDataSourceImpl(
            packageFile = File(PACKAGES_FILE_PATH),
            packageParser = get(),
            csvFileLoader = get(),
            appDispatchers = get()
        )
    }

    single<LocalVehicleDataSource> {
        CsvVehicleDataSourceImpl(
            vehicleFile = File(FLEET_FILE_PATH),
            fleetParser = get(),
            csvFileLoader = get(),
            appDispatchers = get()
        )
    }

    single<LocalRouteDataSource> {
        CsvRouteDataSourceImpl(
            routeFile = File(ROUTES_FILE_PATH),
            routeParser = get(),
            csvFileLoader = get(),
            appDispatchers = get()
        )
    }

    single<RemoteWarehouseDataSource> {
        SupabaseWarehouseDataSourceImpl(
            client = get(),
            retryTemplate = get(),
            circuitBreaker = get(),
            appDispatchers = get()
        )
    }

    single<RemotePackageDataSource> {
        SupabasePackageDataSourceImpl(
            client = get(),
            retryTemplate = get(),
            circuitBreaker = get(),
            appDispatchers = get()
        )
    }

    single<RemoteVehicleDataSource> {
        SupabaseVehicleDataSourceImpl(
            client = get(),
            retryTemplate = get(),
            circuitBreaker = get(),
            appDispatchers = get()
        )
    }

    single<RemoteRouteDataSource> {
        SupabaseRouteDataSourceImpl(
            client = get(),
            retryTemplate = get(),
            circuitBreaker = get(),
            appDispatchers = get()
        )
    }


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
