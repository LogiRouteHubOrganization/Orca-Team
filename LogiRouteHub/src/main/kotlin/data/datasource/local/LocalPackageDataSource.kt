package data.datasource.local

import data.dataholder.LogisticsPackageRaw

interface LocalPackageDataSource {

    fun loadPackages(): List<LogisticsPackageRaw>

    fun addPackage(logisticsPackage: LogisticsPackageRaw)

    fun updatePackage(logisticsPackage: LogisticsPackageRaw)

    fun deletePackage(packageId: String)

    fun replacePackages(packages: List<LogisticsPackageRaw>)
}
