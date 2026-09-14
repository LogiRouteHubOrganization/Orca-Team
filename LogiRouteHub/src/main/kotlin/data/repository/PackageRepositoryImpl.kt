package data.repository

import data.datasource.PackageDataSource
import data.mapper.PackageMapper
import domain.model.Package
import domain.repository.PackageRepository
import domain.repository.WarehouseRepository

class PackageRepositoryImpl(
    private val packageDataSource: PackageDataSource,
    private val packageMapper: PackageMapper,
    private val warehouseRepository: WarehouseRepository
) : PackageRepository {

    companion object {
        private const val NOT_FOUND_INDEX = -1
    }

    private val packages = mutableListOf<Package>()
    private var isLoaded = false

    private suspend fun loadPackagesIfNeeded() {
        if (isLoaded) return

        val rawPackages = packageDataSource.loadPackages()

        for (rawPackage in rawPackages) {
            val originWarehouse =
                warehouseRepository.findById(rawPackage.originHubId)

            val destinationWarehouse =
                warehouseRepository.findById(rawPackage.destinationHubId)

            if (originWarehouse != null && destinationWarehouse != null) {
                val logisticsPackage = packageMapper.mapToDomainModel(
                    rawPackage = rawPackage,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )

                packages.add(logisticsPackage)
                originWarehouse.addPackage(logisticsPackage)
            }
        }

        isLoaded = true
    }

    override suspend fun findById(packageId: String): Package? {
        loadPackagesIfNeeded()
        return packages.find { it.id == packageId }
    }

    override suspend fun findAll(): List<Package> {
        loadPackagesIfNeeded()
        return packages.toList()
    }

    override suspend fun add(logisticsPackage: Package) {
        loadPackagesIfNeeded()

        packages.add(logisticsPackage)
        logisticsPackage.originWarehouse.addPackage(logisticsPackage)
    }

    override suspend fun update(logisticsPackage: Package) {
        loadPackagesIfNeeded()

        val packageIndex =
            packages.indexOfFirst { it.id == logisticsPackage.id }

        if (packageIndex == NOT_FOUND_INDEX) return

        val previousPackage = packages[packageIndex]
        val previousOriginWarehouse = previousPackage.originWarehouse
        val newOriginWarehouse = logisticsPackage.originWarehouse

        if (previousOriginWarehouse.id == newOriginWarehouse.id) {
            val warehousePackageIndex =
                previousOriginWarehouse.cargoQueue
                    .indexOfFirst { it.id == logisticsPackage.id }

            if (warehousePackageIndex != NOT_FOUND_INDEX) {
                previousOriginWarehouse.updatePackageAtIndex(
                    warehousePackageIndex,
                    logisticsPackage
                )
            }
        } else {
            previousOriginWarehouse.removePackage(previousPackage)
            newOriginWarehouse.addPackage(logisticsPackage)
        }

        packages[packageIndex] = logisticsPackage
    }

    override suspend fun delete(packageId: String) {
        loadPackagesIfNeeded()

        val packageIndex =
            packages.indexOfFirst { it.id == packageId }

        if (packageIndex == NOT_FOUND_INDEX) return

        val logisticsPackage = packages[packageIndex]

        logisticsPackage.originWarehouse.removePackage(logisticsPackage)
        packages.removeAt(packageIndex)
    }
}