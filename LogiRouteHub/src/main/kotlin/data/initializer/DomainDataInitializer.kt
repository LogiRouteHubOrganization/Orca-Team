package data.initializer

import data.initializer.dependencies.DomainDataSources
import data.initializer.dependencies.DomainMappers
import data.initializer.dependencies.DomainRepositories

class DomainDataInitializer(
    private val dataSources: DomainDataSources,
    private val mappers: DomainMappers,
    private val repositories: DomainRepositories
) {

    fun initialize() {
        initializeWarehouses()
        initializePackages()
        initializeVehicles()
        initializeRoutes()
    }

    private fun initializeWarehouses() {

        val rawWarehouses = dataSources.warehouseDataSource.loadWarehouses()

        for (rawWarehouse in rawWarehouses) {

            val warehouse = mappers.warehouseMapper.mapToDomainModel(
                rawWarehouse
            )

            repositories.warehouseRepository.add(
                warehouse
            )
        }
    }

    private fun initializePackages() {

        val rawPackages = dataSources.packageDataSource.loadPackages()

        for (rawPackage in rawPackages) {

            val originWarehouse = repositories.warehouseRepository.findById(
                rawPackage.originHubId
            )

            val destinationWarehouse = repositories.warehouseRepository.findById(
                rawPackage.destinationHubId
            )

            if (originWarehouse != null && destinationWarehouse != null) {

                val logisticsPackage = mappers.packageMapper.mapToDomainModel(
                    rawPackage,
                    originWarehouse,
                    destinationWarehouse
                )

                repositories.packageRepository.add(logisticsPackage)
                originWarehouse.addPackage(logisticsPackage)
            }
        }
    }

    private fun initializeVehicles() {

        val rawVehicles =
            dataSources.vehicleDataSource.loadVehicles()

        for (rawVehicle in rawVehicles) {

            val currentWarehouse = repositories.warehouseRepository.findById(
                rawVehicle.currentHubId
            )

            if (currentWarehouse != null) {

                val vehicle = mappers.vehicleMapper.mapToDomainModel(
                    rawVehicle,
                    currentWarehouse
                )

                repositories.vehicleRepository.add(vehicle)

                currentWarehouse.addVehicle(vehicle)
            }
        }
    }

    private fun initializeRoutes() {

        val rawRoutes = dataSources.routeDataSource.loadRoutes()

        for (rawRoute in rawRoutes) {

            val originWarehouse = repositories.warehouseRepository.findById(
                rawRoute.originHubId
            )

            val destinationWarehouse = repositories.warehouseRepository.findById(
                rawRoute.destinationHubId
            )

            if (originWarehouse != null && destinationWarehouse != null) {

                val route = mappers.routeMapper.mapToDomainModel(
                    rawRoute,
                    originWarehouse,
                    destinationWarehouse
                )

                repositories.routeRepository.add(route)

                originWarehouse.addRoute(route)
            }
        }
    }
}

