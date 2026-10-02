package di

import data.datasource.PackageDataSource
import data.datasource.RouteDataSource
import data.datasource.VehicleDataSource
import data.datasource.WarehouseDataSource
import data.datasource.csv.CsvPackageDataSource
import data.datasource.csv.CsvRouteDataSource
import data.datasource.csv.CsvVehicleDataSource
import data.datasource.csv.CsvWarehouseDataSource

import data.mapper.PackageMapper
import data.mapper.RouteMapper
import data.mapper.VehicleMapper
import data.mapper.WarehouseMapper

import data.parser.FleetParser
import data.parser.PackageParser
import data.parser.RouteParser
import data.parser.WarehouseParser

import data.remote.datasource.PackageRemoteDataSource
import data.remote.datasource.RouteRemoteDataSource
import data.remote.datasource.VehicleRemoteDataSource
import data.remote.datasource.WarehouseRemoteDataSource
import data.remote.datasource.supabase.PackageRemoteDataSourceImpl
import data.remote.datasource.supabase.RouteRemoteDataSourceImpl
import data.remote.datasource.supabase.VehicleRemoteDataSourceImpl
import data.remote.datasource.supabase.WarehouseRemoteDataSourceImpl

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
    single<WarehouseDataSource> {
        CsvWarehouseDataSource(
            warehouseFile = File(WAREHOUSES_FILE_PATH),
            warehouseParser = get(),
            localDataSourceValidator = get()
        )
    }

    single<PackageDataSource> {
        CsvPackageDataSource(
            packageFile = File(PACKAGES_FILE_PATH),
            packageParser = get(),
            localDataSourceValidator = get()
        )
    }

    single<VehicleDataSource> {
        CsvVehicleDataSource(
            vehicleFile = File(FLEET_FILE_PATH),
            fleetParser = get(),
            localDataSourceValidator = get()
        )
    }

    single<RouteDataSource> {
        CsvRouteDataSource(
            routeFile = File(ROUTES_FILE_PATH),
            routeParser = get(),
            localDataSourceValidator = get()
        )
    }


    // Remote Data Sources
    single<WarehouseRemoteDataSource> {
        WarehouseRemoteDataSourceImpl(
            retryTemplate = get()
        )
    }

    single<PackageRemoteDataSource> {
        PackageRemoteDataSourceImpl(
            retryTemplate = get()
        )
    }

    single<VehicleRemoteDataSource> {
        VehicleRemoteDataSourceImpl(
            retryTemplate = get()
        )
    }

    single<RouteRemoteDataSource> {
        RouteRemoteDataSourceImpl(
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
