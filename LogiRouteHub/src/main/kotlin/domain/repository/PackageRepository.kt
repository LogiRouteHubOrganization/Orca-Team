package domain.repository

import domain.model.Package

interface PackageRepository {

    suspend fun findById(packageId: String): Package?

    suspend fun findAll(): List<Package>

    suspend fun add(logisticsPackage: Package)

    suspend fun update(logisticsPackage: Package)

    suspend fun delete(packageId: String)
}
