package repository.csv

import dataHolder.LogisticsPackageRaw
import parser.PackageParser
import repository.PackageRepository
import domain.model.Package
import domain.model.Warehouse

class CsvPackageRepository(
    private val packageCsvFilePath: String,
    private val packageParser: PackageParser = PackageParser()
) : PackageRepository {

    override fun fetchAllPackages(): List<Package> {
        val rawPackages: List<LogisticsPackageRaw> =
            packageParser.readLogisticsPackageRowsFromFile(packageCsvFilePath)

        return rawPackages.map { rawPackage ->
            Package(
                id = rawPackage.packageId,
                weightByKg = rawPackage.packageWeightByKg,
                priority = rawPackage.packagePriority,
                originWarehouse = createTemporaryWarehouse(rawPackage.originHubId),
                destinationWarehouse = createTemporaryWarehouse(rawPackage.destinationHubId)
            )
        }
    }

    override fun findPackageById(targetPackageId: String): Package? {
        val normalizedSearchId = targetPackageId.trim()

        return fetchAllPackages().find { packageItem ->
            packageItem.id.equals(normalizedSearchId, ignoreCase = true)
        }
    }

    private fun createTemporaryWarehouse(warehouseId: String): Warehouse {
        return Warehouse(
            id = warehouseId,
            name = "Temp Warehouse $warehouseId",
            regionalZone = "UNKNOWN",
            latitude = 0.0,
            longitude = 0.0          
        )
    }
}