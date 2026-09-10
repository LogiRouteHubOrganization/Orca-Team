package data.datasource

import data.dataholder.LogisticsPackageRaw

interface PackageDataSource {

    fun loadPackages(): List<LogisticsPackageRaw>
}
