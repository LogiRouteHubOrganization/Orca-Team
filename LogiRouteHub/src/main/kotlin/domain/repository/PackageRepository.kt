package domain.repository

import domain.model.Package

interface PackageRepository {

    fun findById(packageId: String): Package?

    fun findAll(): List<Package>

    fun add(logisticsPackage: Package)

    fun update(logisticsPackage: Package)
}
