package repository

import dataHolder.LogisticsPackageRaw

interface PackageRepository {

    fun loadPackages(): List<LogisticsPackageRaw>
}