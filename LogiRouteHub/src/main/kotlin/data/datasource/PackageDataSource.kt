package data.datasource

import data.dataholder.LogisticsPackageRaw

interface PackageDataSource {

    fun loadPackages(): List<LogisticsPackageRaw>

    fun addPackage(logisticsPackage: LogisticsPackageRaw)

    fun updatePackage(logisticsPackage: LogisticsPackageRaw)

    fun deletePackage(packageId: String)

    fun replacePackages(packages: List<LogisticsPackageRaw>)
}
