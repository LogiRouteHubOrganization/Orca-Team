package data.mapper

import data.dataholder.LogisticsPackageRaw
import domain.model.Package
import domain.model.Warehouse

class PackageMapper {

    fun mapToDomainModel(
        rawPackage: LogisticsPackageRaw,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Package {
        return Package(
            id = rawPackage.packageId,
            weightByKg = rawPackage.packageWeightByKg,
            priority = rawPackage.packagePriority,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }
}
