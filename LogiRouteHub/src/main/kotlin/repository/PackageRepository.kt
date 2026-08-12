package repository

import dataHolder.LogisticsPackageRaw

interface PackageRepository {

    // Loads the complete list of raw packages loaded from the storage.
    fun loadPackages(): List<LogisticsPackageRaw>
}