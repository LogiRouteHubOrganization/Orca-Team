package data.repository

import data.dataholder.LogisticsPackageRaw

interface PackageRepository {

    fun loadPackages(): List<LogisticsPackageRaw>
}
