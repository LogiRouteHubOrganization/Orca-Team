package data.datasource.local

import data.dataholder.LogisticsPackageRaw
interface LocalPackageDataSource {

    suspend fun loadPackages(): List<LogisticsPackageRaw>

    suspend fun addPackage(logisticsPackage: LogisticsPackageRaw)

    suspend fun updatePackage(logisticsPackage: LogisticsPackageRaw)

    suspend fun deletePackage(packageId: String)

    suspend fun replacePackages(packages: List<LogisticsPackageRaw>)
}
