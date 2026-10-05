package di

import data.datasource.local.LocalPackageDataSource
import data.datasource.local.LocalRouteDataSource
import data.datasource.local.LocalVehicleDataSource
import data.datasource.local.LocalWarehouseDataSource
import data.datasource.csv.CsvPackageDataSourceImpl
import data.datasource.csv.CsvRouteDataSourceImpl
import data.datasource.csv.CsvVehicleDataSourceImpl
import data.datasource.csv.CsvWarehouseDataSourceImpl

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

    // Local validation
    single {
        LocalDataSourceValidator()
    }


    // Parsers
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


    // Mappers
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
            localDataSourceValidator = get()
        )
    }

    single<LocalPackageDataSource> {
        CsvPackageDataSourceImpl(
            packageFile = File(PACKAGES_FILE_PATH),
            packageParser = get(),
            localDataSourceValidator = get()
        )
    }

    single<LocalVehicleDataSource> {
        CsvVehicleDataSourceImpl(
            vehicleFile = File(FLEET_FILE_PATH),
            fleetParser = get(),
            localDataSourceValidator = get()
        )
    }

    single<LocalRouteDataSource> {
        CsvRouteDataSourceImpl(
            routeFile = File(ROUTES_FILE_PATH),
            routeParser = get(),
            localDataSourceValidator = get()
        )
    }


    // Remote Data Sources
    single<RemoteWarehouseDataSource> {
        SupabaseWarehouseDataSourceImpl(
            client = get(),
            retryTemplate = get()
        )
    }

    single<RemotePackageDataSource> {
        SupabasePackageDataSourceImpl(
            client = get(),
            retryTemplate = get()
        )
    }

    single<RemoteVehicleDataSource> {
        SupabaseVehicleDataSourceImpl(
            client = get(),
            retryTemplate = get()
        )
    }

    single<RemoteRouteDataSource> {
        SupabaseRouteDataSourceImpl(
            client = get(),
            retryTemplate = get()
        )
    }

    // Repositories
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
