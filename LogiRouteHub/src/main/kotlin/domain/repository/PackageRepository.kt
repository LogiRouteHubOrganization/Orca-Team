
package domain.repository

import domain.model.Package
import domain.model.updateinput.PackageUpdateModel

interface PackageRepository {

    suspend fun findById(packageId: String): Result<Package?>

    suspend fun findAll(): Result<List<Package>>

    suspend fun add(logisticsPackage: Package): Result<Package>

    suspend fun update(
        packageId: String,
        updateModel: PackageUpdateModel
    ): Result<Package>

    suspend fun delete(packageId: String): Result<Unit>
}

