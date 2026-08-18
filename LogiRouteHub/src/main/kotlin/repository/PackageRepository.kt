package repository

import dataholder.LogisticsPackageRaw

interface PackageRepository {

    fun loadPackages(): List<LogisticsPackageRaw>
}
