package domain.repository

import domain.model.Package
import domain.model.updateinput.PackageUpdateModel

interface PackageRepository {

    suspend fun findById(packageId: String): Package?

    suspend fun findAll(): List<Package>

    suspend fun add(logisticsPackage: Package): Package

    suspend fun update(packageId: String, updateModel: PackageUpdateModel): Package

    suspend fun delete(packageId: String)
}
